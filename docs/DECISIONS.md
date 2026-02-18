# Technical Decisions Log: CampusFind+

**Version:** 2.0
**Last Updated:** February 17, 2026

This document records all major technical decisions. Each entry includes context, options, rationale, consequences, and revisit criteria. Read this before implementing any feature that involves architecture or data model choices.

---

## Table of Contents

1. [Architecture & Design](#architecture--design) — DEC-001 to DEC-003
2. [Technology Stack](#technology-stack) — DEC-004 to DEC-008
3. [Data & Storage](#data--storage) — DEC-009 to DEC-011
4. [UI/UX](#uiux) — DEC-012 to DEC-013
5. [Performance & Optimization](#performance--optimization) — DEC-014 to DEC-015
6. [Security & Privacy](#security--privacy) — DEC-016 to DEC-017
7. [Deployment & DevOps](#deployment--devops) — DEC-018 to DEC-019
8. [MCO 1 Specific Decisions](#mco-1-specific-decisions) — DEC-020 to DEC-021

---

## Architecture & Design

### DEC-001: MVVM Architecture Pattern
**Date:** February 5, 2026 | **Status:** ✅ Accepted | **Decider:** Team Lead

**Decision:** MVVM with Repository Pattern

**Rationale:** Official Android recommendation. Clean separation of UI/domain/data. Lifecycle-aware. Works naturally with Jetpack Compose and StateFlow. Testable ViewModels.

**Consequences:**
- ✅ Testable, maintainable codebase
- ✅ Reactive UI via StateFlow
- ⚠️ More initial boilerplate than simple approaches

---

### DEC-002: Repository Pattern for Data Layer
**Date:** February 6, 2026 | **Status:** ✅ Accepted | **Decider:** Team Lead

**Decision:** Repository interfaces in domain layer, implementations in data layer.

**Rationale:** ViewModels are data-source agnostic. Easy to swap Room-only (Phase 1) for Room+Firestore (Phase 2) without changing ViewModels. Enables mocking in tests.

**Consequences:**
- ✅ Phase 1 → Phase 2 migration requires only repository impl changes
- ✅ ViewModels untouched when adding cloud sync
- ⚠️ More files to maintain

---

### DEC-003: Offline-First Architecture
**Date:** February 6, 2026 | **Status:** ✅ Accepted | **Decider:** Team Lead

**Decision:** Room database is the Single Source of Truth (SSOT) for all data. UI always reads from Room. Cloud (Phase 2) syncs into Room, never bypasses it.

**Rationale:** MCO 1 requirement — entire demo runs offline. Campus WiFi is unreliable. Room guarantees data availability.

**Consequences:**
- ✅ Full functionality in Airplane Mode
- ✅ Instant UI updates (no network latency)
- ⚠️ Must implement sync logic in Phase 2

---

## Technology Stack

### DEC-004: Kotlin as Primary Language
**Date:** February 5, 2026 | **Status:** ✅ Accepted

**Decision:** Kotlin exclusively — no Java.

**Rationale:** Google's official Android language. Null-safety, coroutines, concise syntax. Required for modern Jetpack libraries.

---

### DEC-005: Jetpack Compose for UI
**Date:** February 6, 2026 | **Status:** ✅ Accepted

**Decision:** Jetpack Compose with Material Design 3.

**Rationale:** Declarative UI. Less code. Native StateFlow integration. Required by MCO specification.

---

### DEC-006: Room Database for Local Storage
**Date:** February 6, 2026 | **Status:** ✅ Accepted

**Decision:** Room with two tables: `users` and `lost_items`.

**Rationale:** Type-safe SQL. Compile-time query verification. Flow support for reactive UI. Required by MCO specification.

**Phase 1 schema:**
```sql
-- users table (new in v2.0)
CREATE TABLE users (
    id               TEXT PRIMARY KEY,
    full_name        TEXT NOT NULL,
    email            TEXT NOT NULL UNIQUE,
    password_hash    TEXT NOT NULL,
    messenger_handle TEXT,
    created_at       INTEGER NOT NULL
);

-- lost_items table
CREATE TABLE lost_items (
    id               TEXT PRIMARY KEY,
    title            TEXT NOT NULL,
    description      TEXT NOT NULL,
    status           TEXT NOT NULL CHECK(status IN ('LOST','FOUND')),
    reported_by      TEXT NOT NULL REFERENCES users(id),
    reported_at      INTEGER NOT NULL,
    last_modified_at INTEGER NOT NULL
);

CREATE INDEX idx_status      ON lost_items(status);
CREATE INDEX idx_reported_by ON lost_items(reported_by);
CREATE INDEX idx_reported_at ON lost_items(reported_at DESC);
```

**Phase 2 migration (v1 → v2):**
```sql
ALTER TABLE lost_items ADD COLUMN sync_status TEXT NOT NULL DEFAULT 'PENDING_SYNC';
ALTER TABLE lost_items ADD COLUMN last_synced_at INTEGER;
CREATE INDEX idx_sync_status ON lost_items(sync_status);
```

---

### DEC-007: Backend Technology — Firebase
**Date:** February 10, 2026 | **Status:** ✅ Accepted | **Decider:** Team Lead

**Decision:** Firebase (Firestore + Firebase Auth + Firebase Hosting for web admin)

**Rationale:** No prior backend experience. MCO 2 timeline is tight. Firebase removes entire backend infrastructure concern. Free Spark tier sufficient. Firestore offline SDK complements Room SSOT strategy.

**Firebase services:**
| Service | Purpose |
|---------|---------|
| Firestore | Cloud database, sync target for Room |
| Firebase Auth | User authentication (Phase 2, replaces local auth) |
| Firebase Hosting | Web admin dashboard hosting |

**Consequences:**
- ✅ MCO 2 deadline risk reduced significantly
- ✅ Auth available out of the box
- ⚠️ Vendor lock-in (acceptable at academic scale)
- ⚠️ NoSQL document model requires mapping from relational Room schema

---

### DEC-008: WorkManager for Background Sync
**Date:** February 6, 2026 | **Status:** ✅ Accepted (Phase 2 only)

**Decision:** WorkManager with 15-minute periodic interval, WiFi-only constraint.

**Rationale:** Google's recommended background work solution. Guaranteed execution. Battery-efficient. Survives process death.

---

## Data & Storage

### DEC-009: UUID for Item IDs
**Date:** February 7, 2026 | **Status:** ✅ Accepted

**Decision:** UUID v4 (`UUID.randomUUID().toString()`) for all primary keys — both users and items.

**Rationale:** Generated offline without server coordination. Zero collision risk. Works with Firestore document IDs. No ID remapping needed when syncing.

---

### DEC-010: Enum Storage as Strings
**Date:** February 7, 2026 | **Status:** ✅ Accepted

**Decision:** Store enums as their `.name` String in Room (e.g. "LOST", "FOUND", "SYNCED").

**Rationale:** Safe from reordering. Readable in DB inspection. Matches JSON API format. No TypeConverter needed.

---

### DEC-011: Timestamps as Long (Unix Epoch Milliseconds)
**Date:** February 7, 2026 | **Status:** ✅ Accepted

**Decision:** `System.currentTimeMillis()` — stored as Long in Room and Firestore.

**Rationale:** Standard mobile API format. Efficient (8 bytes). Easy comparison for conflict resolution. Cross-platform compatible (Firestore, JavaScript).

---

## UI/UX

### DEC-012: Material Design 3 Theme
**Date:** February 7, 2026 | **Status:** ✅ Accepted

**Decision:** Material Design 3 with custom color scheme:
- Primary: `#6c63ff` (deep purple)
- Lost status: `#ff4d6d` (red)
- Found status: `#2dd4a0` (teal/green)
- Dark and light theme both supported

---

### DEC-013: Single Activity Architecture
**Date:** February 7, 2026 | **Status:** ✅ Accepted

**Decision:** Single Activity + Jetpack Compose Navigation.

**NavGraph routes:**
```kotlin
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login      : Screen("login")
    object Register   : Screen("register")
    object Home       : Screen("home")
    object AddItem    : Screen("add_item")
    object Detail     : Screen("detail/{itemId}")
    object Profile    : Screen("profile")
    object Settings   : Screen("settings")
}
```

Auth guard: if `SessionManager.isLoggedIn` → startDestination = Home, else = Login.

---

## Performance & Optimization

### DEC-014: Lazy Loading for Item Lists
**Date:** February 8, 2026 | **Status:** ✅ Accepted

**Decision:** `LazyColumn` fed by a `Flow<List<LostItem>>` from Room DAO.

**Rationale:** Only renders visible items. Auto-updates when Room data changes. No manual refresh needed.

---

### DEC-015: Index Database Columns
**Date:** February 8, 2026 | **Status:** ✅ Accepted

**Decision:** Index `status`, `reported_by`, `reported_at` on `lost_items`. Index `email` on `users`.

**Rationale:** `status` — filtered by filter chips. `reported_by` — ownership queries. `reported_at` — default sort order. `email` — login lookup.

---

## Security & Privacy

### DEC-016: Local Authentication for MCO 1 (UPDATED)
**Date:** February 17, 2026 | **Status:** ✅ Accepted (supersedes original DEC-016)
**Decider:** Team Lead — driven by instructor MCO 1 requirements

**Context:** The original DEC-016 deferred all authentication to Phase 2. The instructor's MCO 1 demo requirement explicitly requires two separate user accounts to be created and switched between during the demo. Authentication must therefore be implemented in Phase 1 using only local Room storage.

**Decision:** Full local authentication in Phase 1 using Room `users` table. Firebase Auth deferred to Phase 2 migration.

**Implementation:**
```kotlin
// Password hashing — Phase 1 minimum
fun hashPassword(password: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(password.toByteArray())
    return hash.joinToString("") { "%02x".format(it) }
}

// Login check
suspend fun login(email: String, password: String): Result<User> {
    val hash = hashPassword(password)
    val entity = userDao.getUserByEmail(email)
        ?: return Result.failure(Exception("No account found"))
    if (entity.passwordHash != hash)
        return Result.failure(Exception("Incorrect password"))
    return Result.success(entity.toDomainModel())
}

// Session persistence
fun saveSession(userId: String, userName: String) {
    prefs.edit()
        .putString(PREF_CURRENT_USER_ID, userId)
        .putString(PREF_CURRENT_USER_NAME, userName)
        .apply()
}
```

**Session data stored in SharedPreferences:**
- `current_user_id` — UUID String
- `current_user_name` — display name

**What changes in Phase 2:**
- Firebase Auth replaces local hashing
- `users` table kept but `password_hash` becomes nullable
- `SessionManager.currentUserId` returns Firebase UID instead of local UUID
- No data migration needed for items (reported_by is just a String ID)

**Consequences:**
- ✅ MCO 1 demo works: two accounts, login/logout, ownership enforcement
- ✅ No network dependency for auth
- ⚠️ Passwords stored as SHA-256 hash (not bcrypt) — acceptable for academic scope
- ⚠️ Phase 2 requires auth migration, but data model is designed to accommodate this

---

### DEC-017: Store Sensitive Data Securely
**Date:** February 8, 2026 | **Status:** ✅ Accepted

**Decision:** JWT/Firebase tokens (Phase 2) stored in `EncryptedSharedPreferences`. Session user ID (Phase 1) stored in regular SharedPreferences (non-sensitive, just a UUID).

---

## Deployment & DevOps

### DEC-018: Git Branching Strategy
**Date:** February 8, 2026 | **Status:** ✅ Accepted

**Decision:** Simplified Git Flow — `main` → `develop` → `feature/*`

```
main (protected — demo-ready builds only)
└── develop (integration)
    ├── feature/101-room-entities
    ├── feature/102-user-dao
    ├── feature/106-local-auth
    ├── feature/112-home-screen
    └── feature/114-detail-ownership
```

---

### DEC-019: Semantic Versioning
**Date:** February 8, 2026 | **Status:** ✅ Accepted

**Version plan:**
- `0.1.0` — Initial setup
- `1.0.0` — MCO 1 submission (full local CRUD + auth)
- `2.0.0` — MCO 2 submission (cloud sync + web admin)

---

## MCO 1 Specific Decisions

### DEC-020: Multi-User Support via Single Room Database
**Date:** February 17, 2026 | **Status:** ✅ Accepted | **Decider:** Team Lead

**Context:** The instructor's demo requires two user accounts to exist simultaneously on one device and for User B to see User A's reports. This must work without any network — purely from local storage.

**Options Considered:**

1. **Separate databases per user**
   - Pros: Complete isolation
   - Cons: User B cannot see User A's items at all — fails the demo requirement

2. **Single shared database, all items visible to all users**
   - Pros: User B naturally sees all items including User A's — matches demo exactly
   - Cons: Must enforce ownership at application layer, not database layer

3. **Single database with row-level access control in SQL**
   - Pros: Database-enforced security
   - Cons: Overly complex for local-only; Room doesn't easily support RLS

**Decision:** Single Room database shared by all local users. All items are readable by any logged-in user. Ownership enforcement is done in the ViewModel/Repository layer using `reported_by == currentUserId`.

**Rationale:**
- Directly satisfies the demo: User B logs in and immediately sees User A's reports from the same DB
- Simple to implement — a standard SQL pattern
- Matches how the cloud version will work (Firestore collection visible to all authenticated users)
- The transition from Phase 1 (local shared DB) to Phase 2 (cloud shared DB) is conceptually identical

**Implementation:**
```kotlin
// In HomeViewModel — ALL items, all users can read
fun observeItems() {
    selectedFilter.flatMapLatest { filter ->
        if (filter == null) repository.getAllItems()
        else repository.getItemsByStatus(filter)
    }.collect { items -> _uiState.update { it.copy(items = items) } }
}

// In DetailViewModel — ownership check
val isOwner: Boolean
    get() = currentItem?.reportedBy == sessionManager.currentUserId
```

**Consequences:**
- ✅ MCO 1 demo steps 9–10 work: User B sees User A's reports
- ✅ Simple, standard, well-understood pattern
- ✅ Phase 2 requires no data model changes for this
- ⚠️ Any logged-in user can read any item (intentional — this is a public lost & found board)

**Revisit Criteria:** If private/hidden reports are ever required (not in scope).

---

### DEC-021: Ownership Enforcement — ViewModel Layer
**Date:** February 17, 2026 | **Status:** ✅ Accepted | **Decider:** Team Lead

**Context:** The instructor will explicitly verify that User B cannot mark User A's item as Found. This ownership rule must be enforced reliably.

**Decision:** Ownership is checked in the ViewModel, not the UI and not the DAO.

**Why not in the UI (Composable)?**
- UI logic can be bypassed by future developers accidentally
- Hard to test without UI instrumentation

**Why not in the DAO?**
- DAOs should only express data access, not business rules
- Would require passing `currentUserId` into every DAO call

**Why in the ViewModel?**
- Business logic belongs in ViewModel / Use Case layer per MVVM
- Easily unit-testable without a UI
- One source of truth for all ownership-related decisions

**Implementation:**
```kotlin
// DetailViewModel.kt
data class DetailUiState(
    val item: LostItem? = null,
    val isOwner: Boolean = false,   // ← computed here, passed to UI
    val isLoading: Boolean = false,
    val error: String? = null
)

fun loadItem(itemId: String) {
    viewModelScope.launch {
        val item = repository.getItemById(itemId)
        val isOwner = item?.reportedBy == sessionManager.currentUserId
        _uiState.update { it.copy(item = item, isOwner = isOwner) }
    }
}

fun markAsFound() {
    val item = _uiState.value.item ?: return
    if (!_uiState.value.isOwner) return  // ← double-check, even if UI hides the button
    viewModelScope.launch {
        repository.updateStatus(item.id, ItemStatus.FOUND)
    }
}
```

**In the Composable:**
```kotlin
// DetailScreen.kt — UI reads isOwner from state, never computes it
if (uiState.isOwner && uiState.item?.status == ItemStatus.LOST) {
    Button(onClick = { viewModel.markAsFound() }) {
        Text("Mark as Found")
    }
}
```

**Consequences:**
- ✅ Business rule is testable in pure unit tests
- ✅ Even if UI is modified, ViewModel rejects unauthorized markAsFound() calls
- ✅ Clean MVVM separation — UI is dumb, ViewModel holds the rule

---

## DI, SOLID & Hilt Decisions

### DEC-022: Hilt as Dependency Injection Framework
**Date:** February 17, 2026 | **Status:** ✅ Accepted | **Decider:** Team Lead
**Instructor requirement:** Dependency Injection, DIP, SOLID Principles, Hilt, Repository Pattern, Hilt Modules must all be demonstrable in the final app.

**Context:** The instructor requires the application to follow SOLID principles and use Dependency Injection via Hilt as part of the assessed software engineering practices. This decision formalises how DI is structured throughout the project and supersedes the earlier "Hilt (optional)" note in the tech stack table.

**Options Considered:**

1. **Manual DI (no framework)**
   - Pros: No setup overhead, full control
   - Cons: Verbose, error-prone, no lifecycle integration, hard to scale

2. **Koin**
   - Pros: Lightweight, Kotlin-first, runtime DI
   - Cons: Runtime errors instead of compile-time, less Android-native

3. **Hilt (Dagger-based)**
   - Pros: Compile-time safety, official Android recommendation, first-class ViewModel support, `@HiltWorker` for WorkManager, instructor-specified
   - Cons: Annotation processing adds build time

**Decision:** **Hilt** is the mandatory DI framework for CampusFind+.

**How SOLID maps to Hilt in this project:**

| Principle | How it is implemented |
|-----------|----------------------|
| **S** — Single Responsibility | One class, one job: `AddItemUseCase` only adds items; `DatabaseModule` only provides DB objects |
| **O** — Open/Closed | `LostItemRepository` interface is closed; new impls (Local → CloudSync) extend without modifying it |
| **L** — Liskov Substitution | Any `LostItemRepository` impl can replace another; ViewModels always receive the interface |
| **I** — Interface Segregation | `LostItemRepository` (CRUD) and `SyncableRepository` (sync) are separate interfaces |
| **D** — Dependency Inversion | `RepositoryModule` binds interfaces to impls; domain never imports data layer |

**Hilt Module responsibility split:**

| Module | File | Provides |
|--------|------|---------|
| `DatabaseModule` | `di/DatabaseModule.kt` | `AppDatabase`, `LostItemDao`, `UserDao` |
| `RepositoryModule` | `di/RepositoryModule.kt` | Binds `LostItemRepository` → `LostItemRepositoryImpl`, `UserRepository` → `UserRepositoryImpl` |
| `AppModule` | `di/AppModule.kt` | `SharedPreferences`, `SessionManager` |
| `NetworkModule` | `di/NetworkModule.kt` | `FirebaseFirestore` (Phase 2 only) |

**`@Binds` vs `@Provides` rule:**
- Use `@Binds` when Hilt can construct the impl via `@Inject constructor` — only the interface binding is needed
- Use `@Provides` when the object requires manual construction (Room builder, Firebase.getInstance(), SharedPreferences)

**Consequences:**
- ✅ Compile-time DI — missing bindings cause build errors, not runtime crashes
- ✅ Phase 1 → Phase 2 migration = one `@Binds` line change in `RepositoryModule`
- ✅ Fully testable — every class accepts its dependencies via constructor, enabling fake/mock injection in tests
- ✅ Satisfies all instructor requirements: DI, DIP, SOLID, Repository Pattern, Hilt Modules
- ⚠️ Requires `kapt` or `ksp` annotation processing — adds ~5-10 seconds to first build
- ⚠️ `@HiltAndroidApp` on Application class and `@AndroidEntryPoint` on MainActivity are mandatory or Hilt silently fails

**Revisit Criteria:** None — Hilt is the instructor-required and industry-standard choice.

---

## Deferred Decisions

| Decision | Deferred Until | Topic |
|----------|---------------|-------|
| DEF-001 | Post-MCO 2 | Image/photo storage (Firebase Storage vs S3) |
| DEF-002 | Post-MCO 2 | Push notifications (FCM) |
| DEF-003 | Post-MCO 2 | Multi-campus support |
| DEF-004 | Phase 2 | Firebase Auth migration from local Room auth |

---

## Review Schedule

- ✅ After major technical decisions — as needed
- ✅ End of Sprint 0 (February 23, 2026)
- ⏳ Before MCO 1 code freeze (April 15, 2026)
- ⏳ Before MCO 2 development starts (May 1, 2026)

**Document Owner:** Team Lead
**Last Review:** February 17, 2026
**Next Review:** February 23, 2026 (Sprint 0 close)
