# Context Document: CampusFind+

**Version:** 2.0
**Last Updated:** February 17, 2026
**Current Phase:** Sprint 0 — Planning & Design (wrapping up)

---

## MCO 1 Instructor Demo Flow — Quick Reference

> The single most important thing to keep in mind during Phase 1 development. Every feature decision must support this exact scenario.

```
Step  1  Launch app → Onboarding (first install)
Step  2  Register User A  (maria@uni.edu / "Maria Santos")
Step  3  Login as User A
Step  4  Create lost item report  ("Black Wallet", description)
Step  5  Verify report appears in User A's Item List
Step  6  Logout User A
Step  7  Register User B  (juan@uni.edu / "Juan dela Cruz")
Step  8  Login as User B
Step  9  Browse Item List → User B SEES User A's report
Step 10  User B views the report detail (read-only, no Mark as Found)
Step 11  Logout User B
Step 12  Login as User A
Step 13  User A opens their report → "Mark as Found" button visible
Step 14  Tap "Mark as Found" → status updates to FOUND instantly
Step 15  Instructor confirms ownership rule enforced
```

**Key things the instructor is verifying:**
1. **CRUD works locally** — Create, Read, Update (status), Delete, all via Room
2. **Multi-user on one device** — two separate accounts, both stored in local `users` table
3. **Shared data** — User B sees User A's reports (same Room DB, no cloud)
4. **Ownership enforcement** — only the reporter can Mark as Found / Edit / Delete
5. **No internet required** — entire demo runs in Airplane Mode

---

## Project Terminology

### Acronyms

| Acronym | Full Term | Definition |
|---------|-----------|------------|
| **SRS** | Software Requirements Specification | Original requirements document |
| **PRD** | Product Requirements Document | Product-focused requirements (v2.0) |
| **MCO** | Major Course Output | Academic project milestone (MCO 1 = local, MCO 2 = cloud) |
| **MVVM** | Model-View-ViewModel | Architecture pattern used |
| **DAO** | Data Access Object | Room database interface |
| **SSOT** | Single Source of Truth | Room database is the SSOT for all data |
| **UDF** | Unidirectional Data Flow | Data flows one way: Event → ViewModel → Repository → DB → Flow → UI |
| **DI** | Dependency Injection | A class receives its dependencies from outside instead of creating them itself |
| **DIP** | Dependency Inversion Principle | High-level modules depend on abstractions (interfaces), not concrete implementations |
| **SOLID** | Single / Open-Closed / Liskov / Interface Segregation / Dependency Inversion | Five OOP design principles required by instructor |
| **API** | Application Programming Interface | Backend service interface (Phase 2) |
| **JWT** | JSON Web Token | Auth token format (not used in Phase 1) |
| **RBAC** | Role-Based Access Control | Admin permission system (Phase 2) |
| **FAB** | Floating Action Button | The ➕ button used to create new reports |

### SOLID Principles — Quick Reference

| Letter | Principle | Rule | CampusFind+ Example |
|--------|-----------|------|---------------------|
| **S** | Single Responsibility | One class = one reason to change | `AddItemUseCase` only adds items; `SessionManager` only manages session |
| **O** | Open/Closed | Open for extension, closed for modification | Add `CloudSyncRepo` without changing `LostItemRepository` interface |
| **L** | Liskov Substitution | Implementations are interchangeable | `LocalRepo` and `CloudSyncRepo` both satisfy `LostItemRepository` |
| **I** | Interface Segregation | Interfaces stay small and focused | `LostItemRepository` (CRUD) separate from `SyncableRepository` (sync) |
| **D** | Dependency Inversion | Depend on abstractions, not concretions | `HomeViewModel` injects `LostItemRepository` interface, never `LostItemRepositoryImpl` |

### Dependency Injection Terms

| Term | Definition |
|------|------------|
| **Dependency Injection (DI)** | Pattern where a class receives its dependencies via its constructor instead of creating them. Hilt automates this in Android. |
| **Hilt** | Official Android DI framework built on Dagger. Generates DI code at compile time. |
| **`@HiltAndroidApp`** | Annotation on `CampusFindApplication`. Required entry point — triggers all Hilt code generation. |
| **`@AndroidEntryPoint`** | Annotation on `MainActivity`. Allows Hilt to inject into the Activity and its Composables. |
| **`@HiltViewModel`** | Annotation on ViewModel classes. Hilt creates the ViewModel and injects all constructor parameters. |
| **`@Inject constructor`** | Tells Hilt it can construct this class automatically by providing all constructor parameters. Used on `RepositoryImpl`, `UseCase`, `SessionManager`. |
| **`@Module`** | A class that tells Hilt how to provide things it cannot construct automatically (DB, interfaces, third-party). |
| **`@InstallIn`** | Declares which Hilt component a module belongs to. `SingletonComponent` = app lifetime. |
| **`@Provides`** | Method in a module that manually constructs and returns a dependency (e.g. Room builder, `Firebase.getInstance()`). |
| **`@Binds`** | Method in a module that maps an interface to its implementation. More efficient than `@Provides` for simple binding. |
| **`@Singleton`** | Scope annotation — Hilt creates only one instance of this dependency for the entire app lifetime. |
| **`SingletonComponent`** | The top-level Hilt component. Dependencies installed here are alive as long as the app process is. |
| **Hilt Module** | A `@Module @InstallIn` class/object that teaches Hilt how to build specific dependencies. CampusFind+ has 4: `DatabaseModule`, `RepositoryModule`, `AppModule`, `NetworkModule`. |
| **`hiltViewModel()`** | Compose function that retrieves a `@HiltViewModel`-annotated ViewModel and injects it into a Composable. |

### Domain-Specific Terms

| Term | Definition |
|------|------------|
| **Lost Item / Report** | An item posted by a user as missing. Stored in `lost_items` table. |
| **Found Item** | A report whose status has been updated to FOUND by the reporter. |
| **Reporter** | The logged-in user who created a report. Identified by `reported_by` (userId). |
| **Item Owner** | Same as Reporter. The user whose `id` matches `reported_by` on the item. |
| **Current User** | The user currently logged in. Id stored in SharedPreferences as `current_user_id`. |
| **Session** | The active login state. Stored in SharedPreferences. Cleared on logout. |
| **Ownership Rule** | `item.reportedBy == currentUserId`. Controls visibility of Edit/Delete/Mark as Found. |
| **Item Status** | Current state: `LOST` or `FOUND` (Phase 1). `RESOLVED` added in Phase 2. |
| **Local Auth** | Phase 1 authentication using Room `users` table — no Firebase, no network. |
| **Sync Status** | Phase 2 only. Local sync state: `SYNCED`, `PENDING_SYNC`, `SYNC_FAILED`. |
| **Offline-First** | Architecture where app works fully without internet. Room is always the data source. |
| **Admin User** | Campus staff with elevated privileges. Phase 2 only, Firebase custom claim. |
| **Claim** | Phase 2 only. A submission by a non-owner asserting they found the item. |
| **Tip** | Phase 2 only. A text message left by any user on a report thread. |
| **Messenger Handle** | Facebook Messenger username collected at registration. Used in Phase 2 for contact deep links. |

### Component Names

#### Phase 1 Components

| Component | File | Purpose |
|-----------|------|---------|
| **OnboardingScreen** | OnboardingScreen.kt | 3-page first-install walkthrough |
| **LoginScreen** | LoginScreen.kt | Local auth — email + password against Room |
| **RegisterScreen** | RegisterScreen.kt | Create account — saves to Room `users` table |
| **HomeScreen** | HomeScreen.kt | Scrollable item list with All/Lost/Found filter chips |
| **AddItemScreen** | AddItemScreen.kt | Form to create a new lost item report |
| **DetailScreen** | DetailScreen.kt | Full item view — ownership-aware (shows/hides Mark as Found) |
| **SettingsScreen** | SettingsScreen.kt | App preferences + logout action |
| **UserProfileScreen** | UserProfileScreen.kt | View own profile, items, trust score |
| **OfflineScreen** | OfflineScreen.kt | Visual indicator when connectivity lost (Phase 2 context) |
| **ItemCard** | ItemCard.kt | Composable card shown in the Home list |
| **StatusBadge** | StatusBadge.kt | LOST (red) / FOUND (green) badge composable |
| **FilterChips** | FilterChips.kt | All / Lost / Found filter row |
| **AppDatabase** | AppDatabase.kt | Room database — version 1, two tables: users + lost_items |
| **UserDao** | UserDao.kt | Insert user, get by email, get by id |
| **LostItemDao** | LostItemDao.kt | Full CRUD on lost_items table |
| **UserRepository** | UserRepositoryImpl.kt | Auth logic abstraction |
| **LostItemRepository** | LostItemRepositoryImpl.kt | Item CRUD abstraction |
| **SessionManager** | SessionManager.kt | Wraps SharedPreferences read/write for current_user_id |

#### Phase 2 Additional Components

| Component | File | Purpose |
|-----------|------|---------|
| **NotificationsScreen** | NotificationsScreen.kt | In-app notification feed |
| **SmartHistoryScreen** | SmartHistoryScreen.kt | User's activity history |
| **TutorialScreen** | TutorialScreen.kt | 6-step interactive overlay tutorial |
| **SyncWorker** | SyncWorker.kt | WorkManager background sync to Firestore |
| **SyncManager** | SyncManager.kt | Schedules/cancels periodic sync |
| **LostItemApi** | LostItemApi.kt | Firestore remote data source |
| **AdminDashboard** | Dashboard.tsx | React web admin — reports table, stats |
| **AdminHistory** | History.tsx | React web admin — audit log |

---

## Naming Conventions

### Kotlin Code

**Classes:**
```kotlin
// ViewModels:  [Feature]ViewModel
LoginViewModel / RegisterViewModel / HomeViewModel / AddItemViewModel / DetailViewModel

// Screens:     [Feature]Screen
LoginScreen / RegisterScreen / HomeScreen / AddItemScreen / DetailScreen

// UI State:    [Feature]UiState
data class HomeUiState(val items: List<LostItem>, val filter: ItemStatus?, ...)
data class DetailUiState(val item: LostItem?, val isOwner: Boolean, ...)

// Entities:    [Model]Entity
UserEntity / LostItemEntity

// DTOs:        [Model]Dto  (Phase 2 only)
LostItemDto

// Use Cases:   [Action][Model]UseCase
GetAllItemsUseCase / AddItemUseCase / UpdateItemStatusUseCase / LoginUseCase
```

**Functions:**
```kotlin
// Composables: PascalCase
@Composable fun ItemCard(...) { }
@Composable fun StatusBadge(...) { }

// Regular functions + suspend functions: camelCase
fun onFilterChanged(filter: ItemStatus?)
suspend fun syncWithCloud()

// Boolean: is/has/can prefix
fun isOwner(item: LostItem): Boolean = item.reportedBy == sessionManager.currentUserId
fun hasActiveSession(): Boolean
```

**State / Flow:**
```kotlin
// Private mutable + public immutable pattern — always
private val _uiState = MutableStateFlow(HomeUiState())
val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

// Constants: UPPER_SNAKE_CASE
const val DATABASE_NAME = "campusfind_db"
const val DATABASE_VERSION = 1            // bump on schema changes
const val PREF_CURRENT_USER_ID = "current_user_id"
const val PREF_CURRENT_USER_NAME = "current_user_name"
const val MAX_TITLE_LENGTH = 100
const val MAX_DESCRIPTION_LENGTH = 500
const val SYNC_INTERVAL_MINUTES = 15L    // Phase 2
```

### Database Naming

```sql
-- Tables:   snake_case plural
users
lost_items

-- Columns:  snake_case
reported_by      -- FK → users.id
reported_at      -- Unix epoch ms (Long)
last_modified_at
sync_status      -- Phase 2

-- PKs:      id (UUID String)
-- FKs:      [referenced_table_singular]_id
-- Indexes:  idx_[column]
CREATE INDEX idx_status       ON lost_items(status);
CREATE INDEX idx_reported_by  ON lost_items(reported_by);
CREATE INDEX idx_reported_at  ON lost_items(reported_at DESC);
```

### Package Structure

```
com.campusfind/
├── ui/
│   ├── screens/
│   │   ├── onboarding/    OnboardingScreen.kt
│   │   ├── login/         LoginScreen.kt + LoginViewModel.kt + LoginUiState.kt
│   │   ├── register/      RegisterScreen.kt + RegisterViewModel.kt + RegisterUiState.kt
│   │   ├── home/          HomeScreen.kt + HomeViewModel.kt + HomeUiState.kt
│   │   ├── additem/       AddItemScreen.kt + AddItemViewModel.kt + AddItemUiState.kt
│   │   ├── detail/        DetailScreen.kt + DetailViewModel.kt + DetailUiState.kt
│   │   ├── profile/       UserProfileScreen.kt + ...
│   │   └── settings/      SettingsScreen.kt + ...
│   ├── components/        ItemCard.kt / StatusBadge.kt / FilterChips.kt
│   ├── theme/             Color.kt / Theme.kt / Type.kt
│   └── navigation/        NavGraph.kt + Screen.kt (sealed class)
├── domain/
│   ├── model/             LostItem.kt / User.kt / ItemStatus.kt / SyncStatus.kt
│   ├── repository/        LostItemRepository.kt (interface) / UserRepository.kt (interface)
│   └── usecase/           GetAllItemsUseCase.kt / AddItemUseCase.kt / LoginUseCase.kt / ...
├── data/
│   ├── local/
│   │   ├── database/      AppDatabase.kt / LostItemDao.kt / UserDao.kt
│   │   │                  LostItemEntity.kt / UserEntity.kt
│   │   └── preferences/   SessionManager.kt
│   ├── remote/            (Phase 2) LostItemApi.kt / FirestoreDataSource.kt
│   ├── repository/        LostItemRepositoryImpl.kt / UserRepositoryImpl.kt
│   └── sync/              (Phase 2) SyncWorker.kt / SyncManager.kt
├── di/                    (if Hilt) AppModule.kt / DatabaseModule.kt
└── CampusFindApplication.kt
```

---

## File Structure — Full Project

```
campusfind/              ← Android app
campusfind-admin/        ← React web admin (Phase 2)
docs/                    ← PRD.md / ARCHITECTURE.md / CONTEXT.md / TASKS.md / DECISIONS.md / EXAMPLES.md
```

---

## Current Project Status

**Phase:** Sprint 0 — Planning complete, development starting

### Completed ✅
- Software Requirements Specification (SRS)
- Product Requirements Document (PRD v2.0)
- Architecture Document
- Context Document (v2.0)
- Backend decision: Firebase (DEC-007)
- MCO 1 demo flow defined and documented
- UI/UX wireframes (14 screens, all nav working)

### In Progress 🔄
- Android Studio project setup (TASK-009)
- Database schema finalization

### Not Started ⏳
- Room database implementation (users + lost_items tables)
- Local auth (LoginViewModel, RegisterViewModel, SessionManager)
- Item List screen (HomeScreen + HomeViewModel)
- Add Item screen
- Detail screen with ownership logic
- Navigation graph
- Testing

### Development Timeline

```
Feb 2026  [████████████] Sprint 0 — Planning & Design       ← NOW
Mar 2026  [            ] Project setup + Room + Auth
Apr 2026  [            ] UI screens + CRUD + MCO 1 demo
May 2026  [            ] Firebase connection + cloud sync
Jun 2026  [            ] Phase 2 features + web admin
Jul 2026  [            ] Polish + final submission
```

---

## Known Issues & Decisions Made

| Topic | Decision | Where Documented |
|-------|----------|-----------------|
| Backend | Firebase (Firestore + Auth) | DEC-007 |
| Auth Phase 1 | Local Room only — email + SHA-256 hashed password | DEC-016 updated |
| Session | SharedPreferences (`current_user_id`) | PRD v2.0 |
| Ownership | `reported_by` field matched to session userId | PRD v2.0 |
| IDs | UUID v4 generated offline | DEC-009 |
| Enum storage | String (enum.name) | DEC-010 |
| Timestamps | Long (Unix epoch ms) | DEC-011 |
| Phase 1 scope | NO cloud, NO Firebase Auth, NO sync, NO claims/tips | PRD v2.0 |
| DI Framework | **Hilt** — mandatory per instructor requirement | DEC-022 |
| SOLID / DIP | Domain interfaces in `domain/`, impls in `data/`, bound by `RepositoryModule` | DEC-022, ARCHITECTURE.md |
| Hilt Modules | `DatabaseModule`, `RepositoryModule`, `AppModule`, `NetworkModule` (Phase 2) | DEC-022, ARCHITECTURE.md |

---

## External Resources

### Android Development
- Jetpack Compose: https://developer.android.com/jetpack/compose
- Room Database: https://developer.android.com/training/data-storage/room
- Navigation Compose: https://developer.android.com/jetpack/compose/navigation
- ViewModel + StateFlow: https://developer.android.com/topic/libraries/architecture/viewmodel
- WorkManager (Phase 2): https://developer.android.com/topic/libraries/architecture/workmanager

### Backend (Phase 2)
- Firebase Firestore: https://firebase.google.com/docs/firestore
- Firebase Auth: https://firebase.google.com/docs/auth/android/start

### Web Admin (Phase 2)
- React: https://react.dev/
- Tailwind CSS: https://tailwindcss.com/docs

### Key Dependencies (build.gradle)
```gradle
// Compose
androidx.compose.ui:ui
androidx.compose.material3:material3

// Room
androidx.room:room-runtime
androidx.room:room-ktx
kapt androidx.room:room-compiler

// Navigation
androidx.navigation:navigation-compose

// ViewModel + Lifecycle
androidx.lifecycle:lifecycle-viewmodel-compose
androidx.lifecycle:lifecycle-runtime-ktx

// Coroutines
org.jetbrains.kotlinx:kotlinx-coroutines-android

// WorkManager (Phase 2)
androidx.work:work-runtime-ktx

// Firebase (Phase 2)
com.google.firebase:firebase-firestore-ktx
com.google.firebase:firebase-auth-ktx
```

---

## Quick Reference — Key Constants

```kotlin
// SessionManager.kt
const val PREF_NAME             = "campusfind_prefs"
const val PREF_CURRENT_USER_ID  = "current_user_id"
const val PREF_CURRENT_USER_NAME = "current_user_name"

// AppDatabase.kt
const val DATABASE_NAME    = "campusfind_db"
const val DATABASE_VERSION = 1

// Constants.kt
const val MAX_TITLE_LENGTH       = 100
const val MAX_DESCRIPTION_LENGTH = 500
const val ITEMS_PER_PAGE         = 20

// Phase 2 only
const val SYNC_INTERVAL_MINUTES = 15L
const val API_BASE_URL          = "https://api.campusfind.com/v1"
```

---

**Document Status:** Living document — update as development progresses
**Next Review:** End of Android project setup (March 2026)
