# CampusFind+ Task Board

**Last Updated:** February 17, 2026
**Current Sprint:** Sprint 0 — Planning & Foundation (ending Feb 23)
**Next Sprint:** Sprint 1 — Room Database + Local Auth (starting Mar 1)

---

## MCO 1 Demo Requirements — Task Summary

> Before any Sprint 1 task is marked Done, ask: "Does this support the instructor's 15-step demo?"

```
Register User A → Login → Create report → Logout
Register User B → Login → SEE User A's report → View detail → Logout
Login User A → Open own report → Mark as Found ✓
```

All of this must work with **Airplane Mode ON**.

---

## ✅ Sprint 0 — Completed Tasks

- [x] **TASK-001** Create Software Requirements Specification
- [x] **TASK-002** Create Product Requirements Document (v2.0)
- [x] **TASK-003** Create Architecture Document
- [x] **TASK-004** Create Context Document (v2.0)
- [x] **TASK-005** Finalize Backend Decision → **Firebase** (DEC-007)
- [x] **TASK-006** UI/UX Wireframes — 14 screens, all nav working
- [x] **TASK-007** MCO 1 demo flow defined and documented in PRD v2.0

---

## 🔄 Sprint 0 — Remaining (due Feb 23)

- [x] **TASK-008** Set Up Git Repository
  - Init repo with README, .gitignore (Android + Node)
  - Branch protection on `main` and `develop`
  - Team members added

- [x] **TASK-009** Set Up Android Studio Project
  - Kotlin + Compose, minSdk 26
  - Package structure: ui / domain / data / di
  - All dependencies added to build.gradle
  - Project builds clean

- [x] **TASK-010** Configure Code Quality Tools
  - ktlint + detekt
  - Pre-commit hooks

---

## ⏳ Sprint 1 — Room Database + Local Auth (Mar 1 – Mar 28)

> Goal: A working local-only app where two users can register, log in, post items, and the reporter can mark their item as Found — all from Room, no network.

### 🔴 Critical — Hilt Setup (Do This First — Everything Depends On It)

> Hilt must be configured before any ViewModel, Repository, or UseCase is written. If Hilt is not set up, nothing can be injected.

- [x] **TASK-100** Add Hilt to Gradle and Configure Entry Points
  - Add Hilt plugin to `build.gradle.kts` (project + app level)
  - Add `hilt-android`, `hilt-compiler`, `hilt-navigation-compose` dependencies
  - Annotate `CampusFindApplication` with `@HiltAndroidApp`
  - Annotate `MainActivity` with `@AndroidEntryPoint`
  - **Verify:** Clean build with zero Hilt errors before proceeding
  - **Acceptance:** App launches; no `@HiltAndroidApp` missing error in logcat

- [x] **TASK-100a** Create `di/DatabaseModule.kt`
  - `@Module @InstallIn(SingletonComponent::class) object DatabaseModule`
  - `@Provides @Singleton provideDatabase()` — Room builder with `campusfind_db`
  - `@Provides @Singleton provideLostItemDao(db)` — returns `db.lostItemDao()`
  - `@Provides @Singleton provideUserDao(db)` — returns `db.userDao()`
  - **Acceptance:** DAOs injectable; DAO unit tests compile

- [x] **TASK-100b** Create `di/AppModule.kt`
  - `@Provides @Singleton provideSharedPreferences()` — `getSharedPreferences("campusfind_prefs", MODE_PRIVATE)`
  - `@Provides @Singleton provideSessionManager(prefs)` — passes SharedPreferences in
  - **Acceptance:** `SessionManager` injectable anywhere via `@Inject`

- [x] **TASK-100c** Create `di/RepositoryModule.kt`
  - `@Module @InstallIn(SingletonComponent::class) abstract class RepositoryModule`
  - `@Binds @Singleton bindLostItemRepository(impl: LostItemRepositoryImpl): LostItemRepository`
  - `@Binds @Singleton bindUserRepository(impl: UserRepositoryImpl): UserRepository`
  - **This is where DIP is enforced** — ViewModels receive `LostItemRepository` (interface), Hilt injects `LostItemRepositoryImpl` (concrete)
  - **Acceptance:** Changing the `@Binds` target is the only step needed to swap implementations

- [x] **TASK-100d** Create `di/NetworkModule.kt` (Phase 2 stub only)
  - Create the file with a comment: `// Phase 2 — add FirebaseFirestore provider here`
  - Do NOT add Firebase dependencies yet
  - **Purpose:** Reserves the correct location so Phase 2 starts cleanly

### 🔴 Critical — Database Layer

- [x] **TASK-101** Define Room Entities
  - `UserEntity` — id (UUID), full_name, email, password_hash, messenger_handle (nullable), created_at
  - `LostItemEntity` — id (UUID), title, description, status, reported_by (FK→users), reported_at, last_modified_at
  - Add `@ForeignKey(entity = UserEntity::class)` on `reported_by`
  - **Acceptance:** Entities compile, schema generates correctly

- [x] **TASK-102** Implement UserDao
  - `insertUser(user: UserEntity)`
  - `getUserByEmail(email: String): UserEntity?`
  - `getUserById(id: String): UserEntity?`
  - **Acceptance:** Unit tests pass for all three operations

- [x] **TASK-103** Implement LostItemDao
  - `insertItem(item: LostItemEntity)`
  - `getAllItems(): Flow<List<LostItemEntity>>`
  - `getItemsByStatus(status: String): Flow<List<LostItemEntity>>`
  - `getItemById(id: String): LostItemEntity?`
  - `getItemsByUser(userId: String): Flow<List<LostItemEntity>>`
  - `updateItemStatus(id: String, status: String, timestamp: Long)`
  - `deleteItem(id: String)`
  - **Acceptance:** All queries verified with Room in-memory test DB

- [x] **TASK-104** Create AppDatabase
  - Version 1, two entities: UserEntity + LostItemEntity
  - Singleton pattern
  - `fallbackToDestructiveMigration()` for development
  - **Acceptance:** Database creates without crash on fresh install

- [x] **TASK-105** Implement SessionManager
  - Wraps SharedPreferences
  - `saveSession(userId, userName)` / `clearSession()` / `currentUserId: String?` / `isLoggedIn: Boolean`
  - **Acceptance:** Session persists across app restarts; clears fully on logout

### 🔴 Critical — Authentication (Demo Steps 2–3, 7–8, 12)

- [x] **TASK-106** Implement UserRepository + UserRepositoryImpl + Auth UseCases
  - **Interface** `domain/repository/UserRepository.kt` — `register()`, `login()`, `getUserById()`
  - **Impl** `data/repository/UserRepositoryImpl.kt` — `@Inject constructor(userDao: UserDao)`
  - **`LoginUseCase`** — `@Inject constructor(repo: UserRepository)` — validates credentials, returns `Result<User>`
  - **`RegisterUseCase`** — `@Inject constructor(repo: UserRepository)` — hashes password, inserts user, returns `Result<User>`
  - Bound in `RepositoryModule` via `@Binds`
  - **Acceptance:** Duplicate email rejected; wrong password returns error; SRP satisfied (each use case does one thing)

- [x] **TASK-107** Build RegisterScreen + RegisterViewModel
  - Fields: Full Name, University Email, Password (+ strength bar), Confirm Password, Messenger Username (optional, @ prefix, blue tint)
  - Screen scrollable (`verticalScroll`) — 6 fields overflow 680px
  - Validation: all required fields present, passwords match, email format valid
  - On success: save session, navigate to Home, clear back stack
  - **Acceptance:** Demo step 2 (Register User A) and step 7 (Register User B) pass

- [x] **TASK-108** Build LoginScreen + LoginViewModel
  - Fields: Email, Password (+ eye toggle)
  - On success: save session via SessionManager, navigate to Home
  - On failure: show inline error "Invalid email or password"
  - **Acceptance:** Demo steps 3 and 8 and 12 pass

- [x] **TASK-109** Implement Logout
  - Settings screen → Logout button
  - Calls `SessionManager.clearSession()`
  - Navigate to Login, clear entire back stack (`popUpTo(0)`)
  - **Acceptance:** Demo steps 6 and 11 pass; back button cannot return to Home after logout

- [ ] **TASK-110** Auth Guard in NavGraph
  - On app launch: check `SessionManager.isLoggedIn`
  - If true → start at HomeScreen
  - If false → start at LoginScreen
  - **Acceptance:** App reopens to correct screen after device restart

### 🔴 Critical — Item CRUD (Demo Steps 4–5, 9–10, 13–14)

- [x] **TASK-111** Implement LostItemRepository + LostItemRepositoryImpl
  - **Interface** in `domain/repository/LostItemRepository.kt` — pure Kotlin, zero Android imports
  - **Impl** in `data/repository/LostItemRepositoryImpl.kt` — `@Inject constructor(dao: LostItemDao, sessionManager: SessionManager)`
  - Methods: `getAllItems()`, `getItemsByStatus()`, `getItemById()`, `addItem()`, `updateStatus()`, `deleteItem()`
  - Include entity↔domain mapping functions (`toDomain()`, `toEntity()`) as private extension functions
  - **Bound by `RepositoryModule`** — ViewModel never imports the Impl class directly
  - **Acceptance:** All operations reflected immediately in UI via Flow; DIP satisfied (ViewModel imports only interface)

- [x] **TASK-112** Build HomeScreen + HomeViewModel (Demo steps 5, 9)
  - LazyColumn of ItemCards, all users' items visible to all logged-in users
  - Filter chips: All / Lost / Found — reactive via Flow
  - FAB (➕) navigates to AddItemScreen
  - Empty state when no items
  - **Acceptance:** User B logs in and sees User A's report without any sync

- [x] **TASK-113** Build AddItemScreen + AddItemViewModel (Demo step 4)
  - Fields: Title (required, max 100 chars) + Description (required, max 500 chars)
  - `reportedBy` set to `SessionManager.currentUserId` automatically
  - `reportedAt` = `System.currentTimeMillis()`
  - On submit: insert to Room, navigate back to Home
  - **Acceptance:** Demo step 4 — report appears in list immediately after posting

- [x] **TASK-114** Build DetailScreen + DetailViewModel (Demo steps 10, 13–14)
  - Show: item photo placeholder, title, status badge, description, reporter name, timestamp
  - **Ownership check:** `isOwner = item.reportedBy == sessionManager.currentUserId`
  - If `isOwner` AND status == LOST → show "Mark as Found" button (purple)
  - If `isOwner` → show ⋮ overflow menu with Edit and Delete
  - If NOT owner → read-only view, no action buttons
  - **Acceptance:**
    - Demo step 10: User B sees detail with no action buttons ✓
    - Demo step 13: User A sees "Mark as Found" button on their own item ✓

- [x] **TASK-115** Implement "Mark as Found" (Demo step 14)
  - DetailViewModel.markAsFound(itemId) → calls repository.updateStatus(id, FOUND)
  - UI updates immediately via StateFlow (no refresh needed)
  - StatusBadge changes from red LOST to green FOUND
  - "Mark as Found" button disappears after status change
  - **Acceptance:** Demo step 14 passes; status persists after app restart

- [x] **TASK-116** Implement Delete Report (⋮ menu)
  - Overflow menu visible only to item owner
  - Shows confirmation dialog: "Delete this report? This cannot be undone."
  - On confirm: `repository.deleteItem(id)` → navigate back to Home
  - Item disappears from all users' lists immediately
  - **Acceptance:** Delete works; non-owner cannot delete

### 🟡 High — Remaining Phase 1 Screens

- [ ] **TASK-117** Build UserProfileScreen
  - Shows: name, email, joined date, items posted, trust score (local calculation)
  - Lists user's own items with status badges
  - Quick actions: Post Item, Browse All
  - **Phase 1 only** — no sync badge, no Messenger verified badge

- [ ] **TASK-118** Build OnboardingScreen
  - 3 pages: Welcome / How It Works / Community Guidelines
  - Shown only once on first install (SharedPreferences flag)
  - Skip button on all pages
  - After completion → LoginScreen

- [ ] **TASK-119** Build SettingsScreen
  - Logout button (primary action)
  - App version display
  - Dark/Light theme toggle (optional)

- [ ] **TASK-120** Build ItemCard Composable
  - Item emoji/icon placeholder, title, description preview (2 lines), status badge, timestamp, reporter name
  - "Mark as Found" quick-action only if owner + status LOST

- [ ] **TASK-121** Build StatusBadge Composable
  - LOST → red background, red text
  - FOUND → green background, green text

- [ ] **TASK-122** Build FilterChips Composable
  - All / Lost / Found
  - Single selection; tapping active chip deselects (shows All)
  - Calls HomeViewModel.onFilterChanged()

- [ ] **TASK-123** Set Up NavGraph
  - Single Activity, Compose Navigation
  - Routes: onboarding / login / register / home / additem / detail/{itemId} / profile / settings
  - Auth guard at root (TASK-110)

- [ ] **TASK-124** Implement Material Design 3 Theme
  - Primary color: deep purple (#6c63ff)
  - Lost status color: #ff4d6d
  - Found status color: #2dd4a0
  - Light + Dark theme support
  - Dynamic color optional

### 🟢 Medium — Quality & Polish

- [ ] **TASK-125** Unit Tests — ViewModels
  - HomeViewModel filter logic
  - DetailViewModel isOwner logic
  - AddItemViewModel validation

- [ ] **TASK-126** Unit Tests — Repository
  - Insert + query + update + delete operations on in-memory Room DB

- [ ] **TASK-127** UI Tests — Key Demo Flow
  - Instrumented test that runs the 15-step demo scenario end to end
  - Verifies User B cannot see Mark as Found on User A's item

- [ ] **TASK-129** SOLID & Hilt Verification Checklist
  - [ ] **S** — Every class has exactly one responsibility (no ViewModel touching DAO directly)
  - [ ] **O** — `LostItemRepository` interface unchanged between Phase 1 and Phase 2 builds
  - [ ] **L** — `LocalLostItemRepository` is a valid substitute for `LostItemRepository` in all contexts
  - [ ] **I** — `SyncableRepository` is a separate interface from `LostItemRepository`
  - [ ] **D** — No class in `ui/` or `domain/` imports anything from `data/` except through interfaces
  - [ ] **Hilt** — All 4 modules present and clean: `DatabaseModule`, `RepositoryModule`, `AppModule`, `NetworkModule` stub
  - [ ] **Hilt** — `@HiltAndroidApp` on Application, `@AndroidEntryPoint` on MainActivity, `@HiltViewModel` on all ViewModels
  - [ ] **Repository** — `RepositoryModule` uses `@Binds` (not `@Provides`) for interface bindings
  - **Acceptance:** Instructor can trace the DI graph: Hilt Module → Impl → Interface → UseCase → ViewModel

- [ ] **TASK-128** MCO 1 Demo Rehearsal
  - Run full 15-step flow on a physical device
  - Run in Airplane Mode to confirm offline operation
  - Time each step — total demo should be under 5 minutes

---

## ⏳ Sprint 2 — Firebase + Cloud (May 1 – May 31)

> Builds on top of the complete MCO 1 app. Room stays as SSOT. Firebase is added as sync target.

- [ ] **TASK-201** Connect Firebase to Android project (google-services.json)
- [ ] **TASK-202** Implement Firebase Auth (replace local auth, migrate existing accounts)
- [ ] **TASK-203** Implement Firestore data source (LostItemApi)
- [ ] **TASK-204** Implement SyncWorker (WorkManager, WiFi-only, 15-min interval)
- [ ] **TASK-205** Implement SyncManager (schedule / cancel)
- [ ] **TASK-206** Add `sync_status` + `last_synced_at` columns to LostItemEntity (DB migration v1→v2)
- [ ] **TASK-207** Conflict resolution — last-write-wins using `last_modified_at`

---

## ⏳ Sprint 3 — Phase 2 Features (Jun 1 – Jun 20)

- [ ] **TASK-301** Claims feature — submit claim with photo proof
- [ ] **TASK-302** Public tips — leave text tips on any report
- [ ] **TASK-303** Messenger deep link — open Messenger app with reporter's handle
- [ ] **TASK-304** Notifications — local notifications for own report activity
- [ ] **TASK-305** Smart History screen — user activity log
- [ ] **TASK-306** Notifications screen
- [ ] **TASK-307** Interactive Tutorial (6-step overlay, first Phase 2 launch)

---

## ⏳ Sprint 4 — Web Admin + Polish (Jun 21 – Jul 7)

- [ ] **TASK-401** React admin project setup (Vite + Tailwind)
- [ ] **TASK-402** Admin Dashboard — stat cards + reports table
- [ ] **TASK-403** Admin History — audit log with color-coded entries
- [ ] **TASK-404** Admin auth — Firebase custom claims for admin role
- [ ] **TASK-405** Deploy web admin to Firebase Hosting
- [ ] **TASK-406** Final APK — signed release build
- [ ] **TASK-407** MCO 2 demo rehearsal
- [ ] **TASK-408** Final documentation package

---

## Definition of Done

A task is **Done** when:
- ✅ Code written, follows style guide (ktlint passes)
- ✅ Unit tests written and passing (where applicable)
- ✅ Code reviewed (or self-reviewed for solo project)
- ✅ Commits follow format: `type(scope): message`
- ✅ Branch merged to `develop`
- ✅ Acceptance criteria met
- ✅ No regressions introduced
- ✅ Supports the MCO 1 demo flow (Sprint 1 tasks)

---

## Commit Message Format

```
feat(auth):     Add login screen with Room authentication
feat(home):     Show all users' items in LazyColumn
feat(detail):   Add ownership check for Mark as Found button
fix(session):   Clear back stack on logout
test(repo):     Add unit tests for LostItemRepository CRUD
chore(db):      Create Room entities for users and lost_items
```

---

## Important Dates

| Date | Event |
|------|-------|
| Feb 23, 2026 | Sprint 0 ends — all docs finalized |
| Mar 1, 2026  | Sprint 1 starts — Room + Auth |
| Apr 15, 2026 | MCO 1 feature complete (code freeze) |
| Apr 30, 2026 | **MCO 1 Presentation** |
| May 1, 2026  | Sprint 2 starts — Firebase |
| Jun 30, 2026 | **MCO 2 Presentation** |
| Jul 7, 2026  | Final submission |
