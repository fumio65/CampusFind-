package com.campusfind.data.remote.source

import com.campusfind.data.remote.dto.ClaimReplyDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

class ClaimReplyRemoteDataSource @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun upsert(dto: ClaimReplyDto) {
        supabase.from("claim_replies").upsert(dto)
    }

    suspend fun fetchAll(): List<ClaimReplyDto> {
        return supabase.from("claim_replies").select().decodeList()
    }
}
