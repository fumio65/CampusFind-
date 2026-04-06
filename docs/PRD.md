# Product Requirements Document: CampusFind+

**Version:** 2.0
**Platform:** Android (API 26+)
**Tech Stack:** Kotlin, Jetpack Compose, Room, MVVM
**Last Updated:** February 17, 2026

---

## Product Objective

Provide a lightweight mobile application for university students to report, browse, and manage lost and found items on campus. Phase 1 delivers a fully functional local-only app with multi-user support via a local Room database. Phase 2 adds cloud synchronization, web administration, and community interaction features.
KO
---

## MCO 1 — Instructor Demo Flow (LOCAL DATABASE ONLY)

> This is the exact scenario the instructor will walk through during the MCO 1 presentation. Every feature listed here must work completely offline using Room as the only data store.

### Demo Sequence

```
1. Launch app → Onboarding (first install only)
2. Register as User A  (e.g. "Maria Santos", maria@uni.edu)
3. Log in as User A
4. Create a lost item report  (e.g. "Black Wallet", description, timestamp auto-set)
5. Verify the report appears in the Item List under User A's session
6. Log out of User A
7. Register as User B  (e.g. "Juan dela Cruz", juan@uni.edu)
8. Log in as User B
9. Browse the Item List → User B can see the report posted by User A
10. User B interacts with the report  (views detail; leaves the item as LOST)
11. Log out of User B
12. Log in as User A
13. User A opens their own report → taps "Mark as Found"
14. Item status changes to FOUND with visual indicator
15. Instructor confirms only the reporter (User A) can mark their own item as Found
```

### MCO 1 Feature Checklist

| # | Feature | Requirement |
|---|---------|-------------|
| 1 | **User Registration** | Full Name, University Email, Password, Confirm Password, Messenger Username (optional). Stored locally in Room (`users` table). |
| 2 | **User Login / Logout** | Authenticate against local `users` table. Session persisted via SharedPreferences (`current_user_id`). Logout clears session. |
| 3 | **Create Report (C)** | Logged-in user posts a lost item: Title, Description. `reportedBy` (userId) and `reportedAt` (timestamp) set automatically. Status defaults to LOST. |
| 4 | **Browse All Reports (R)** | All users see all reports in a scrollable list regardless of who posted them. Filter chips: All / Lost / Found. |
| 5 | **View Report Detail (R)** | Any user can tap an item card to see full details: title, description, status badge, reporter name, timestamp. |
| 6 | **Mark as Found (U)** | Only the reporter (userId matches `current_user_id`) sees the "Mark as Found" button. Tapping it updates status to FOUND in Room and refreshes UI instantly via Flow. |
| 7 | **Delete Report (D)** | Reporter can delete their own report via the ⋮ overflow menu → Delete → confirmation dialog. Removed from Room and disappears from all users' lists. |
| 8 | **Ownership Enforcement** | Edit, Delete, and Mark as Found controls are hidden for items not owned by the current user. Non-owners see read-only detail. |
| 9 | **Offline-Only** | No network calls. All data lives in Room. App must function with Airplane Mode on. |
| 10 | **Status Filter** | Filter chips on Home screen filter the Room query by status in real time via Flow. |

---

## MCO 2 — All Core Features + Cloud

> MCO 2 builds on top of the complete MCO 1 app. All MCO 1 features continue to work. Cloud sync is added as an enhancement layer, not a replacement.

### MCO 2 Additional Features

| Feature | Description |
|---------|-------------|
| **Cloud Sync** | WorkManager syncs local Room data to Firestore in background (WiFi only). |
| **Multi-Device Visibility** | Reports posted on one device are visible on another after sync. |
| **Submit Claims** | Any user can submit a claim with photo proof on another user's lost item. Reporter receives notification. |
| **Public Tips** | Community members can leave text tips on any report. |
| **Messenger Integration** | When reporter approves a claim, "Message on Messenger" deep link appears using the reporter's saved Messenger username. |
| **Notifications** | Local notifications for status changes and new tips on user's own reports. |
| **Web Admin Dashboard** | React web interface for campus staff: view all reports, filter, flag, delete, view audit log. |
| **Interactive Tutorial** | 6-step overlay tutorial covering all features, shown on first Phase 2 launch. |

---

## Data Model

### Phase 1 — Room Tables

**users**
```
id            String  PRIMARY KEY (UUID)
full_name     String
email         String  UNIQUE
password_hash String
messenger_handle String (nullable)
created_at    Long
```

**lost_items**
```
id              String  PRIMARY KEY (UUID)
title           String
description     String
status          String  CHECK('LOST','FOUND')
reported_by     String  FOREIGN KEY → users.id
reported_at     Long
last_modified_at Long
```

**DAO Operations:**
- `insertUser` / `getUserByEmail` / `getUserById`
- `insertItem` / `getAllItems(): Flow` / `getItemsByStatus(): Flow`
- `getItemById` / `getItemsByUser`
- `updateItemStatus(id, status, timestamp)`
- `deleteItem(id)`

### Phase 2 — Additional Fields on lost_items

```
sync_status     String  DEFAULT 'PENDING_SYNC'  ('SYNCED','PENDING_SYNC','SYNC_FAILED')
last_synced_at  Long    (nullable)
```

---

## Architecture

**Pattern:** MVVM with Repository Pattern

**Layers:**
- **UI** — Jetpack Compose screens and Composable components
- **ViewModel** — StateFlow for reactive UI state, one ViewModel per screen
- **Repository** — Single abstraction over Room (Phase 1) + Firestore (Phase 2)
- **Data / Local** — Room database, DAOs, Entities
- **Data / Remote** — Firebase Firestore + Auth (Phase 2 only)

**Key Screens (Phase 1):**
1. Onboarding (3 pages, first install only)
2. Login
3. Register
4. Item List (Home) with filter chips
5. Add Item Form
6. Item Detail (ownership-aware controls)
7. Settings
8. User Profile
9. Offline State indicator

**Additional Screens (Phase 2):**
10. Notifications
11. Smart History
12. Interactive Tutorial
13. Admin Dashboard (web)
14. Admin History (web)

---

## Ownership Rules (Critical for MCO 1)

```
canMarkAsFound(item)  = item.reportedBy == currentUserId
canDelete(item)       = item.reportedBy == currentUserId
canEdit(item)         = item.reportedBy == currentUserId
canViewDetail(item)   = true  (any logged-in user)
canBrowseList()       = true  (any logged-in user)
```

These rules are enforced in the ViewModel layer, never just in the UI.

---

## Session Management (Phase 1)

```kotlin
// On successful login:
SharedPreferences.edit()
    .putString("current_user_id", user.id)
    .putString("current_user_name", user.fullName)
    .apply()

// On logout:
SharedPreferences.edit().clear().apply()
// Navigate to Login screen, clear back stack

// On app launch:
if (prefs.getString("current_user_id", null) != null) {
    navigate to HomeScreen
} else {
    navigate to LoginScreen
}
```

---

## Success Metrics

| Metric | Target |
|--------|--------|
| MCO 1 Demo | All 15 demo steps complete without crash |
| Offline reliability | 100% — all CRUD works in Airplane Mode |
| Item posting time | < 500 ms |
| List load time | < 2 s |
| MCO 2 sync success rate | > 95% |
| Cloud match rate | 40%+ of lost items marked as found |

---

## Technical Constraints

- **Offline-First (Phase 1)**: Zero network dependency. All features work with no internet.
- **Authentication (Phase 1)**: Local only. No Firebase Auth. Password stored as hash (SHA-256 minimum).
- **Storage**: < 50 MB app size.
- **Android Version**: Minimum API 26 (Android 8.0).
- **Architecture**: Strict MVVM — no business logic in Composables or DAOs.

---

## Out of Scope — Phase 1 (MCO 1)

- Cloud sync of any kind
- Firebase or any network calls
- Push notifications
- Photo attachments
- Claim submission / community tips
- Web admin dashboard
- Multi-campus support

---

## Out of Scope — Phase 2 (MCO 2)

- In-app chat (Messenger deep link used instead)
- Location / map integration
- Item claiming verification beyond photo proof
- Multi-campus support

---

## Development Timeline

| Milestone | Target Date | Deliverable |
|-----------|-------------|-------------|
| Sprint 0 complete | Feb 23, 2026 | All docs, decisions finalized |
| Project setup | Mar 1, 2026 | Android Studio project, Gradle, Room |
| MCO 1 feature complete | Apr 15, 2026 | All Phase 1 features working locally |
| MCO 1 demo-ready | Apr 30, 2026 | Full instructor demo flow rehearsed |
| Backend live | May 15, 2026 | Firebase Firestore + Auth connected |
| MCO 2 feature complete | Jun 20, 2026 | All Phase 2 features working |
| MCO 2 demo-ready | Jun 30, 2026 | Full app demo + web admin |
| Final submission | Jul 7, 2026 | Signed APK + full documentation |

---

## Deliverables

**MCO 1:**
- Android Studio project (source code)
- Debug APK installable on instructor's device
- Room database schema diagram
- MVVM architecture diagram
- Demo script (the 15-step flow above)

**MCO 2 (additional):**
- Signed APK
- Firebase project access (or live URL)
- Web admin dashboard (Firebase Hosting)
- API / Firestore schema documentation
- User documentation
- Final architecture diagram

---

## Open Questions (Resolved)

| Question | Resolution |
|----------|------------|
| Authentication in Phase 1? | **Local Room-based auth** — email + hashed password in `users` table |
| Backend choice? | **Firebase** (Firestore + Auth) — decided DEC-007 |
| Item ownership enforcement? | **reportedBy field** matched against SharedPreferences session |
| Data retention? | 90 days for FOUND items (Phase 2); no auto-expiry in Phase 1 |
| Admin roles? | Manual Firebase custom claims for admin role (Phase 2) |
