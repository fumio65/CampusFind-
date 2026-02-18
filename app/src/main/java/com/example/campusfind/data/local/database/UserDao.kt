package com.campusfind.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * FILE: app/src/main/java/com/campusfind/data/local/database/UserDao.kt
 *
 * Data Access Object for the `users` table.
 *
 * Why only 3 methods:
 * - insertUser     → Registration (TASK-107 RegisterViewModel)
 * - getUserByEmail → Login lookup — email is the unique identifier (TASK-108 LoginViewModel)
 * - getUserById    → Load reporter name on item detail screen (TASK-114 DetailViewModel)
 *
 * Why suspend functions (not Flow):
 * - User data is fetched once per action (login, register) — not observed over time
 * - Flow is used for lists that need to auto-update (LostItemDao) — not needed here
 *
 * Why OnConflictStrategy.ABORT on insert:
 * - If the same email is inserted twice, the DB throws an exception
 * - The unique index on email (UserEntity) enforces this at the DB level
 * - RegisterUseCase catches this exception and returns a "Email already exists" error
 *
 * See: DEC-006 (Room), DEC-015 (indexes), TASK-102
 */
@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)
    // ABORT — throws SQLiteConstraintException if email already exists
    // RegisterUseCase catches this and returns Result.failure("Email already in use")

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?
    // Used by LoginUseCase to find the account and verify the password hash
    // Returns null if no account found → "No account found" error in LoginUseCase

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?
    // Used by DetailViewModel to show the reporter's full name on the item detail screen
    // Returns null if user was deleted (CASCADE would have deleted their items too)
}