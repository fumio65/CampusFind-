package com.campusfind.data.remote.source

import com.campusfind.data.remote.dto.LostItemDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

class LostItemRemoteDataSource @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun upsert(dto: LostItemDto) {
        supabase.from("lost_items").upsert(dto)
    }

    suspend fun fetchAll(): List<LostItemDto> {
        return supabase.from("lost_items").select().decodeList()
    }
}
