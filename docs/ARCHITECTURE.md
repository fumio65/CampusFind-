# Architecture Document: CampusFind+

**Version:** 2.0
**Last Updated:** February 17, 2026

---

## System Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     Android App                         │
│                                                         │
│  ┌──────────┐   ┌──────────┐   ┌──────────────────┐   │
│  │ Compose  │──▶│ViewModel │──▶│   Repository     │   │
│  │   UI     │◀──│(Hilt VM) │   │   (interface)    │   │
│  └──────────┘   └──────────┘   └────────┬─────────┘   │
│                                          │              │
│                               ┌──────────┴──────────┐  │
│                               │  RepositoryImpl     │  │
│                               │  (injected by Hilt) │  │
│                               └──────┬──────────────┘  │
│                                      │                  │
│                          ┌───────────┴───────────┐      │
│                          │                       │      │
│                     ┌────▼────┐           ┌──────▼───┐  │
│                     │  Room   │           │Firestore │  │
│                     │  (DAOs) │           │(Phase 2) │  │
│                     └─────────┘           └──────────┘  │
└─────────────────────────────────────────────────────────┘
         ▲
         │ Hilt provides all dependencies top-to-bottom
```

### MVVM + Hilt Architecture Flow

```
User Event
    │
    ▼
Composable Screen          ← UI Layer: only observes state, triggers events
    │  collectAsState()
    ▼
ViewModel  (@HiltViewModel) ← Presentation Layer: holds UI state, calls use cases
    │  inject: UseCase
    ▼
Use Case                    ← Domain Layer: single business action, pure Kotlin
    │  inject: Repository (interface)
    ▼
Repository Interface        ← Domain boundary: defines WHAT, not HOW  ← DIP lives here
    │
    ▼
RepositoryImpl              ← Data Layer: concrete implementation, injected by Hilt
    │  inject: DAO, SessionManager
    ▼
Room DAO / Firestore        ← Data sources: provided by Hilt modules
```

**Unidirectional Data Flow:**
```
Event → ViewModel → UseCase → Repository → DataSource
                                                │
                     UI ← StateFlow ← ViewModel ◀── Flow<T>
```

---

## SOLID Principles in CampusFind+

> These are the five design principles the instructor requires. Every class in the project must follow them. Each principle is shown with its direct application in this codebase.

### S — Single Responsibility Principle (SRP)

**Rule:** Every class has exactly one reason to change.

| Class | Single Responsibility |
|-------|----------------------|
| `LostItemDao` | Only executes SQL queries on the `lost_items` table |
| `LostItemRepositoryImpl` | Only coordinates between local DAO and remote Firestore |
| `AddItemViewModel` | Only manages UI state for the Add Item screen |
| `AddItemUseCase` | Only handles the business logic of creating one item |
| `SessionManager` | Only reads/writes the current user session in SharedPreferences |
| `UserEntity` | Only maps to the `users` database table |

**Violation example to avoid:**
```kotlin
// ❌ WRONG — ViewModel doing database access directly
class HomeViewModel : ViewModel() {
    private val db = AppDatabase.getDatabase(context) // SRP violation
    fun loadItems() { db.lostItemDao().getAllItems() } // also DIP violation
}

// ✅ CORRECT — ViewModel depends only on a use case
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllItemsUseCase: GetAllItemsUseCase
) : ViewModel() {
    fun loadItems() { getAllItemsUseCase() }
}
```

---

### O — Open/Closed Principle (OCP)

**Rule:** Open for extension, closed for modification.

Applied in CampusFind+ via the Repository interface:

```kotlin
// The interface is CLOSED for modification
interface LostItemRepository {
    fun getAllItems(): Flow<List<LostItem>>
    suspend fun addItem(item: LostItem)
    suspend fun updateStatus(id: String, status: ItemStatus)
    suspend fun deleteItem(id: String)
}

// Phase 1: EXTEND by writing a new implementation — no interface change
class LocalLostItemRepository @Inject constructor(
    private val dao: LostItemDao
) : LostItemRepository { ... }

// Phase 2: EXTEND again by writing another implementation — still no interface change
class CloudSyncLostItemRepository @Inject constructor(
    private val dao: LostItemDao,
    private val firestore: FirebaseFirestore
) : LostItemRepository { ... }

// Switching Phase 1 → Phase 2: change ONE line in the Hilt module
// Nothing else in the app changes
```

---

### L — Liskov Substitution Principle (LSP)

**Rule:** Any implementation can replace its interface without breaking the program.

```kotlin
// ViewModel never knows WHICH implementation it has — both are valid substitutes
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LostItemRepository // interface, not impl
) : ViewModel() {
    // Works identically whether Hilt injects LocalRepo or CloudSyncRepo
}
```

This is why ViewModels and Use Cases always depend on the **interface** (in `domain/`), never the concrete `Impl` class (in `data/`).

---

### I — Interface Segregation Principle (ISP)

**Rule:** No class should be forced to implement methods it doesn't use. Keep interfaces small and focused.

```kotlin
// ❌ WRONG — one fat interface forces all implementations to handle sync
interface LostItemRepository {
    fun getAllItems(): Flow<List<LostItem>>
    suspend fun addItem(item: LostItem)
    suspend fun syncWithCloud(): Result<Unit> // Phase 1 impl doesn't need this!
    suspend fun uploadPendingItems(): Result<Unit> // Phase 1 impl doesn't need this!
}

// ✅ CORRECT — split into focused interfaces
interface LostItemRepository {           // core CRUD — used by all
    fun getAllItems(): Flow<List<LostItem>>
    suspend fun addItem(item: LostItem)
    suspend fun updateStatus(id: String, status: ItemStatus)
    suspend fun deleteItem(id: String)
}

interface SyncableRepository {           // sync — Phase 2 only
    suspend fun syncWithCloud(): Result<Unit>
    suspend fun uploadPendingItems(): Result<Unit>
}

// Phase 1 impl: only implements LostItemRepository
class LocalLostItemRepository @Inject constructor(...) : LostItemRepository

// Phase 2 impl: implements both
class CloudSyncLostItemRepository @Inject constructor(...) :
    LostItemRepository, SyncableRepository
```

---

### D — Dependency Inversion Principle (DIP)

**Rule:** High-level modules must not depend on low-level modules. Both must depend on abstractions (interfaces).

This is the most important principle for CampusFind+ because it is what makes Hilt work correctly.

```
HIGH-LEVEL (ViewModel, UseCase)
        │
        │ depends on ▼
ABSTRACTION (LostItemRepository interface  ← in domain/ package)
        │
        │ implemented by ▼
LOW-LEVEL (LostItemRepositoryImpl, UserRepositoryImpl ← in data/ package)
```

**Concrete rule for this project:**
- `domain/` package has **zero imports** from `data/` or `ui/`
- `ui/` (ViewModels) imports from `domain/` only — never from `data/`
- `data/` imports from `domain/` to implement its interfaces

```kotlin
// domain/repository/LostItemRepository.kt
// ✅ Pure interface — no Room, no Firestore, no Android imports
interface LostItemRepository {
    fun getAllItems(): Flow<List<LostItem>>
    suspend fun addItem(item: LostItem)
}

// data/repository/LostItemRepositoryImpl.kt
// ✅ Impl knows about Room — but domain and UI don't
class LostItemRepositoryImpl @Inject constructor(
    private val lostItemDao: LostItemDao,       // data layer detail
    private val sessionManager: SessionManager   // data layer detail
) : LostItemRepository {
    override fun getAllItems() = lostItemDao.getAllItems().map { it.toDomainModel() }
}

// ui/home/HomeViewModel.kt
// ✅ Only sees the interface — completely unaware of Room or Firestore
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LostItemRepository  // abstraction, not impl
) : ViewModel()
```

---

## Dependency Injection with Hilt

### What is Dependency Injection?

Dependency Injection (DI) means: **a class receives its dependencies from the outside instead of creating them itself.**

```kotlin
// ❌ WITHOUT DI — class creates its own dependencies (tightly coupled, untestable)
class HomeViewModel : ViewModel() {
    private val db = Room.databaseBuilder(...).build()       // creates DB
    private val dao = db.lostItemDao()                       // creates DAO
    private val repo = LostItemRepositoryImpl(dao)           // creates repo
}

// ✅ WITH DI — dependencies are injected by Hilt (loosely coupled, testable)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LostItemRepository  // Hilt provides this
) : ViewModel()
```

### Why Hilt?

Hilt is the official Android DI framework built on top of Dagger. In this project it:

1. Provides `AppDatabase`, `LostItemDao`, `UserDao` as singletons — one instance across the whole app
2. Provides `LostItemRepositoryImpl` and `UserRepositoryImpl` as the concrete implementations behind the domain interfaces
3. Provides `SessionManager` wherever it is needed
4. Injects into `@HiltViewModel` so ViewModels get their dependencies automatically
5. Injects into `SyncWorker` via `@HiltWorker` (Phase 2)

### Hilt Setup — Required Files

#### 1. Application Class

```kotlin
// CampusFindApplication.kt
package com.campusfind

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp   // ← triggers Hilt code generation for the whole app
class CampusFindApplication : Application()
```

```xml
<!-- AndroidManifest.xml — register the application class -->
<application
    android:name=".CampusFindApplication"
    ... >
```

#### 2. MainActivity

```kotlin
// MainActivity.kt
@AndroidEntryPoint   // ← required on every Activity that uses Hilt
class MainActivity : ComponentActivity() {
    override fun onCreate(...) {
        super.onCreate(savedInstanceState)
        setContent { CampusFindApp() }
    }
}
```

---

## Hilt Modules

Hilt Modules tell Hilt **how to construct** things it cannot construct automatically (like interfaces, Room databases, and third-party classes).

### Module 1 — DatabaseModule

**File:** `di/DatabaseModule.kt`
**Scope:** `@Singleton` — one instance for the app lifetime
**Provides:** `AppDatabase`, `LostItemDao`, `UserDao`

```kotlin
// di/DatabaseModule.kt
package com.campusfind.di

import android.content.Context
import androidx.room.Room
import com.campusfind.data.local.database.AppDatabase
import com.campusfind.data.local.database.LostItemDao
import com.campusfind.data.local.database.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)   // lives as long as the app
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "campusfind_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideLostItemDao(database: AppDatabase): LostItemDao {
        return database.lostItemDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}
```

---

### Module 2 — RepositoryModule

**File:** `di/RepositoryModule.kt`
**Purpose:** Binds the concrete `Impl` classes to their domain interfaces — this is where DIP is enforced
**Scope:** `@Singleton`

```kotlin
// di/RepositoryModule.kt
package com.campusfind.di

import com.campusfind.data.repository.LostItemRepositoryImpl
import com.campusfind.data.repository.UserRepositoryImpl
import com.campusfind.domain.repository.LostItemRepository
import com.campusfind.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // This single line is what DIP looks like in Hilt:
    // "When anyone asks for LostItemRepository, give them LostItemRepositoryImpl"
    @Binds
    @Singleton
    abstract fun bindLostItemRepository(
        impl: LostItemRepositoryImpl   // concrete class in data/
    ): LostItemRepository              // interface in domain/

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository
}
```

> **Key point:** `@Binds` is used (not `@Provides`) because Hilt can construct the `Impl` class itself via `@Inject constructor`. `@Provides` is for things Hilt can't construct (like Room, third-party classes).

---

### Module 3 — AppModule

**File:** `di/AppModule.kt`
**Purpose:** Provides app-wide utilities — `SessionManager`, `SharedPreferences`
**Scope:** `@Singleton`

```kotlin
// di/AppModule.kt
package com.campusfind.di

import android.content.Context
import android.content.SharedPreferences
import com.campusfind.data.local.preferences.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("campusfind_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideSessionManager(prefs: SharedPreferences): SessionManager {
        return SessionManager(prefs)   // Hilt injects prefs, which it already knows how to make
    }
}
```

---

### Module 4 — NetworkModule (Phase 2 only)

**File:** `di/NetworkModule.kt`
**Purpose:** Provides Firebase Firestore instance
**Scope:** `@Singleton`

```kotlin
// di/NetworkModule.kt  — Phase 2 only, do not create in Phase 1
package com.campusfind.di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}
```

---

## Hilt Component Scopes — Quick Reference

| Scope | Component | Lifetime | Use for |
|-------|-----------|----------|---------|
| `@Singleton` | `SingletonComponent` | App lifetime | Database, Repository, SessionManager |
| `@ActivityScoped` | `ActivityComponent` | Activity lifetime | Rarely used with Compose |
| `@ViewModelScoped` | `ViewModelComponent` | ViewModel lifetime | Use Cases scoped to one screen |
| `@ActivityRetainedScoped` | `ActivityRetainedComponent` | Survives rotation | Shared state between screens |

**In this project:** everything is `@Singleton`. Use cases can optionally be `@ViewModelScoped` if they hold state.

---

## Hilt File Structure

```
app/src/main/java/com/campusfind/
│
├── di/                                   ← ALL Hilt modules live here
│   ├── AppModule.kt                      @Provides: SharedPreferences, SessionManager
│   ├── DatabaseModule.kt                 @Provides: AppDatabase, LostItemDao, UserDao
│   ├── RepositoryModule.kt               @Binds: interfaces → implementations
│   └── NetworkModule.kt                  @Provides: FirebaseFirestore (Phase 2 only)
│
├── domain/                               ← Pure Kotlin, zero Android/Hilt imports
│   ├── model/
│   │   ├── LostItem.kt                   domain data class
│   │   ├── User.kt                       domain data class
│   │   ├── ItemStatus.kt                 enum: LOST, FOUND
│   │   └── SyncStatus.kt                 enum: SYNCED, PENDING_SYNC, SYNC_FAILED
│   ├── repository/
│   │   ├── LostItemRepository.kt         interface  ← DIP boundary
│   │   └── UserRepository.kt             interface  ← DIP boundary
│   └── usecase/
│       ├── GetAllItemsUseCase.kt          @Inject constructor(repo: LostItemRepository)
│       ├── AddItemUseCase.kt              @Inject constructor(repo, session)
│       ├── UpdateItemStatusUseCase.kt     @Inject constructor(repo, session)
│       ├── DeleteItemUseCase.kt           @Inject constructor(repo, session)
│       ├── LoginUseCase.kt                @Inject constructor(repo: UserRepository)
│       └── RegisterUseCase.kt             @Inject constructor(repo: UserRepository)
│
├── data/                                 ← Android-aware, injected by Hilt modules
│   ├── local/
│   │   ├── database/
│   │   │   ├── AppDatabase.kt            @Database — provided by DatabaseModule
│   │   │   ├── LostItemDao.kt            @Dao — provided by DatabaseModule
│   │   │   ├── UserDao.kt                @Dao — provided by DatabaseModule
│   │   │   ├── LostItemEntity.kt         @Entity
│   │   │   └── UserEntity.kt             @Entity
│   │   └── preferences/
│   │       └── SessionManager.kt         @Inject constructor(prefs: SharedPreferences)
│   ├── remote/                           (Phase 2 only)
│   │   └── FirestoreDataSource.kt        @Inject constructor(firestore: FirebaseFirestore)
│   └── repository/
│       ├── LostItemRepositoryImpl.kt     @Inject constructor(dao, session) : LostItemRepository
│       └── UserRepositoryImpl.kt         @Inject constructor(dao) : UserRepository
│
└── ui/                                   ← Hilt entry points
    ├── screens/
    │   ├── login/
    │   │   ├── LoginScreen.kt            no Hilt annotation needed
    │   │   └── LoginViewModel.kt         @HiltViewModel @Inject constructor(loginUseCase)
    │   ├── register/
    │   │   ├── RegisterScreen.kt
    │   │   └── RegisterViewModel.kt      @HiltViewModel @Inject constructor(registerUseCase)
    │   ├── home/
    │   │   ├── HomeScreen.kt
    │   │   └── HomeViewModel.kt          @HiltViewModel @Inject constructor(getAllItems, session)
    │   ├── additem/
    │   │   ├── AddItemScreen.kt
    │   │   └── AddItemViewModel.kt       @HiltViewModel @Inject constructor(addItemUseCase)
    │   └── detail/
    │       ├── DetailScreen.kt
    │       └── DetailViewModel.kt        @HiltViewModel @Inject constructor(getById, updateStatus, delete, session)
    └── MainActivity.kt                   @AndroidEntryPoint
```

---

## Repository Pattern — Full Explanation

The Repository Pattern is the **bridge between the domain layer and the data layer**. It follows DIP: the ViewModel knows only the interface; Hilt injects the implementation.

### Why Repository Pattern?

Without it:
```kotlin
// ❌ ViewModel knows about Room — tightly coupled, untestable, violates DIP
class HomeViewModel : ViewModel() {
    private val dao = AppDatabase.getInstance().lostItemDao()
    fun loadItems() = dao.getAllItems()
}
```

With it:
```kotlin
// ✅ ViewModel knows nothing about Room or Firestore
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LostItemRepository  // interface only
) : ViewModel() {
    fun loadItems() = repository.getAllItems()
}
```

### Repository Interface (Domain Layer)

```kotlin
// domain/repository/LostItemRepository.kt
package com.campusfind.domain.repository

import com.campusfind.domain.model.LostItem
import com.campusfind.domain.model.ItemStatus
import kotlinx.coroutines.flow.Flow

// This interface is the contract. It lives in domain/ — pure Kotlin.
// It defines WHAT the app can do with items, not HOW.
interface LostItemRepository {
    fun getAllItems(): Flow<List<LostItem>>
    fun getItemsByStatus(status: ItemStatus): Flow<List<LostItem>>
    suspend fun getItemById(id: String): LostItem?
    suspend fun addItem(title: String, description: String, reportedBy: String)
    suspend fun updateStatus(id: String, status: ItemStatus)
    suspend fun deleteItem(id: String)
}
```

```kotlin
// domain/repository/UserRepository.kt
package com.campusfind.domain.repository

import com.campusfind.domain.model.User

interface UserRepository {
    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        messengerHandle: String?
    ): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun getUserById(id: String): User?
}
```

### Repository Implementation (Data Layer)

```kotlin
// data/repository/LostItemRepositoryImpl.kt
package com.campusfind.data.repository

import com.campusfind.data.local.database.LostItemDao
import com.campusfind.data.local.database.LostItemEntity
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import com.campusfind.domain.repository.LostItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

// @Inject constructor — Hilt knows how to build this automatically
// because all parameters (LostItemDao, SessionManager) are also injectable
class LostItemRepositoryImpl @Inject constructor(
    private val dao: LostItemDao,
    private val sessionManager: SessionManager
) : LostItemRepository {

    override fun getAllItems(): Flow<List<LostItem>> =
        dao.getAllItems().map { list -> list.map { it.toDomain() } }

    override fun getItemsByStatus(status: ItemStatus): Flow<List<LostItem>> =
        dao.getItemsByStatus(status.name).map { list -> list.map { it.toDomain() } }

    override suspend fun getItemById(id: String): LostItem? =
        dao.getItemById(id)?.toDomain()

    override suspend fun addItem(title: String, description: String, reportedBy: String) {
        val entity = LostItemEntity(
            id              = UUID.randomUUID().toString(),
            title           = title,
            description     = description,
            status          = "LOST",
            reportedBy      = reportedBy,
            reportedAt      = System.currentTimeMillis(),
            lastModifiedAt  = System.currentTimeMillis()
        )
        dao.insertItem(entity)
    }

    override suspend fun updateStatus(id: String, status: ItemStatus) {
        dao.updateItemStatus(id, status.name, System.currentTimeMillis())
    }

    override suspend fun deleteItem(id: String) {
        dao.deleteItem(id)
    }

    // Mapping function — keeps domain model clean of Room annotations
    private fun LostItemEntity.toDomain() = LostItem(
        id             = id,
        title          = title,
        description    = description,
        status         = ItemStatus.valueOf(status),
        reportedBy     = reportedBy,
        reportedAt     = reportedAt,
        lastModifiedAt = lastModifiedAt
    )
}
```

---

## Use Cases (Single Responsibility + DIP in action)

Each Use Case does exactly one thing. This satisfies SRP and keeps ViewModels thin.

```kotlin
// domain/usecase/AddItemUseCase.kt
package com.campusfind.domain.usecase

import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.repository.LostItemRepository
import javax.inject.Inject

// @Inject constructor — Hilt injects both dependencies automatically
class AddItemUseCase @Inject constructor(
    private val repository: LostItemRepository,  // interface — DIP satisfied
    private val sessionManager: SessionManager
) {
    // operator fun invoke = call the use case like a function: addItemUseCase(title, desc)
    suspend operator fun invoke(title: String, description: String): Result<Unit> {
        val userId = sessionManager.currentUserId
            ?: return Result.failure(Exception("Not logged in"))
        if (title.isBlank()) return Result.failure(Exception("Title is required"))
        if (description.isBlank()) return Result.failure(Exception("Description is required"))
        return try {
            repository.addItem(title.trim(), description.trim(), userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

```kotlin
// domain/usecase/UpdateItemStatusUseCase.kt
class UpdateItemStatusUseCase @Inject constructor(
    private val repository: LostItemRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(itemId: String, item: LostItem): Result<Unit> {
        if (item.reportedBy != sessionManager.currentUserId)
            return Result.failure(Exception("Not authorised"))
        return try {
            repository.updateStatus(itemId, ItemStatus.FOUND)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## ViewModel with Hilt

```kotlin
// ui/home/HomeViewModel.kt
package com.campusfind.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import com.campusfind.domain.usecase.GetAllItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val items: List<LostItem> = emptyList(),
    val selectedFilter: ItemStatus? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// @HiltViewModel = Hilt manages this ViewModel's lifecycle and injects dependencies
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllItems: GetAllItemsUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _filter = MutableStateFlow<ItemStatus?>(null)

    init { observeItems() }

    private fun observeItems() {
        viewModelScope.launch {
            _filter
                .flatMapLatest { filter -> getAllItems(filter) }
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { items -> _uiState.update { it.copy(items = items) } }
        }
    }

    fun onFilterChanged(filter: ItemStatus?) {
        _filter.value = filter
        _uiState.update { it.copy(selectedFilter = filter) }
    }
}
```

```kotlin
// In any Composable — Hilt creates and provides the ViewModel automatically
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    // ...
}
```

---

## Design Patterns Summary

| Pattern | Where Applied | Why |
|---------|--------------|-----|
| **MVVM** | All screens | Separation of UI / state / data; lifecycle-aware |
| **Repository** | `domain/repository/` interfaces, `data/repository/` impls | DIP — ViewModel never touches Room or Firestore directly |
| **Use Cases** | `domain/usecase/` | SRP — one class per business action; keeps ViewModels thin |
| **Dependency Injection** | Hilt throughout | Loose coupling; testable; SOLID-compliant wiring |
| **Offline-First / SSOT** | Room as the only UI data source | Reliability; deterministic UI |
| **UDF** | StateFlow → Composable | Predictable, debuggable, one-directional data flow |
| **Observer** | `Flow` from DAO → ViewModel → UI | Reactive updates without polling |

---

## Hilt Gradle Setup

```kotlin
// project-level build.gradle.kts
plugins {
    id("com.google.dagger.hilt.android") version "2.51" apply false
}

// app-level build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")          // for Room annotation processing
    id("com.google.dagger.hilt.android")   // Hilt plugin
    id("org.jetbrains.kotlin.kapt")        // for Hilt annotation processing
}

dependencies {
    // Hilt
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-compiler:2.51")

    // Hilt for Compose navigation
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // WorkManager + Hilt (Phase 2)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}
```

---

## Testing with Hilt

Because of DIP and DI, every layer is independently testable.

```kotlin
// Unit test — ViewModel with a fake repository (no Hilt, no Android)
class HomeViewModelTest {
    private val fakeRepository = FakeLostItemRepository()  // test double
    private val fakeSession    = FakeSessionManager()
    private val viewModel      = HomeViewModel(
        GetAllItemsUseCase(fakeRepository),
        fakeSession
    )

    @Test
    fun `items load correctly`() = runTest {
        fakeRepository.emit(listOf(testItem))
        assertEquals(1, viewModel.uiState.value.items.size)
    }
}

// Integration test — real Room, in-memory database
@RunWith(AndroidJUnit4::class)
class LostItemDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: LostItemDao

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        dao = db.lostItemDao()
    }

    @After fun teardown() { db.close() }

    @Test fun insertAndQuery() = runTest {
        dao.insertItem(testEntity)
        val items = dao.getAllItems().first()
        assertEquals(1, items.size)
    }
}
```

---

## Phase 1 → Phase 2 Migration (Hilt makes this safe)

When adding cloud sync in Phase 2, the **only** change is in `RepositoryModule.kt`:

```kotlin
// Phase 1 — binds local implementation
@Binds abstract fun bindLostItemRepository(
    impl: LocalLostItemRepository
): LostItemRepository

// Phase 2 — swap to cloud-syncing implementation (one line change)
@Binds abstract fun bindLostItemRepository(
    impl: CloudSyncLostItemRepository
): LostItemRepository
```

**Zero changes** to ViewModels, Use Cases, Composables, or domain models. This is DIP + DI + OCP working together.

---

## Open Technical Decisions (Resolved)

| Topic | Decision |
|-------|----------|
| DI Framework | **Hilt** (DEC-022) — mandatory per instructor requirement |
| Backend | **Firebase** (DEC-007) |
| Auth Phase 1 | **Local Room** (DEC-016) |
| Ownership enforcement | **ViewModel layer** (DEC-021) |
| Multi-user | **Single shared Room DB** (DEC-020) |
