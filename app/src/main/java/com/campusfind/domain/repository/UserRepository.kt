package com.campusfind.domain.repository

import com.campusfind.domain.model.User

interface UserRepository {

    // Auth
    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        messengerHandle: String?
    ): Result<User>

    suspend fun login(email: String, password: String): Result<User>

    // Query
    suspend fun getUserById(id: String): User?

    // Flow observer — emits on every Room write so UI updates instantly
    fun observeUser(id: String): kotlinx.coroutines.flow.Flow<User?>

    // Updates
    suspend fun updateMessengerHandle(userId: String, messengerHandle: String): Result<Unit>

    // NEW: update name + email, syncs to cloud
    suspend fun updateProfile(userId: String, fullName: String, email: String): Result<Unit>

    // NEW: update profile photo, syncs to cloud
    suspend fun updateProfilePhoto(userId: String, photoUri: String): Result<Unit>
}