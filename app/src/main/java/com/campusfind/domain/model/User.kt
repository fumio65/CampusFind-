package com.campusfind.domain.model

/**
 * FILE: app/src/main/java/com/campusfind/domain/model/User.kt
 *
 * Domain model representing a user in the app.
 * Pure Kotlin data class — no Room annotations, no Android dependencies.
 *
 * Why this exists separate from UserEntity:
 * - UserEntity is a data layer concern — it has @Entity, @ColumnInfo, database field names
 * - User is a domain model — ViewModels and UseCases work with this, never with UserEntity
 * - This separation satisfies DIP — the domain layer has zero dependency on Room
 *
 * Why passwordHash is excluded:
 * - Once a user is logged in, the password hash is never needed again
 * - Exposing it in the domain model increases security risk if accidentally logged
 * - Authentication happens only in UserRepository — after that, only id and name matter
 *
 * See: DEC-001 (MVVM), DEC-002 (Repository), DEC-022 (DIP), TASK-106
 */
data class User(
    val id: String,              // UUID from UserEntity.id
    val fullName: String,        // UserEntity.fullName
    val email: String,           // UserEntity.email
    val messengerHandle: String?, // UserEntity.messengerHandle (nullable)
    val createdAt: Long          // UserEntity.createdAt (Unix epoch ms)
)