package com.campusfind.data.remote.source

import com.campusfind.data.remote.dto.TipDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

class TipRemoteDataSource @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun upsert(dto: TipDto) {
        supabase.from("tips").upsert(dto)
    }

    suspend fun fetchAll(): List<TipDto> {
        return supabase.from("tips").select().decodeList()
    }
}
