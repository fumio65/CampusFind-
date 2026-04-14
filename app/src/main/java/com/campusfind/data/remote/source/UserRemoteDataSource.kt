package com.campusfind.data.remote.source

import com.campusfind.data.remote.dto.UserDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun upsert(dto: UserDto) {
        supabase. from("users").upsert(dto)
    }

    suspend fun fetchAll(): List<UserDto> {
        return supabase.from("users").select().decodeList()
    }
}