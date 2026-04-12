package com.campusfind.data.remote.source

import com.campusfind.data.remote.dto.ClaimDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

class ClaimRemoteDataSource @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun upsert(dto: ClaimDto) {
        supabase.from("claims").upsert(dto)
    }

    suspend fun fetchAll(): List<ClaimDto> {
        return supabase.from("claims").select().decodeList()
    }
}
