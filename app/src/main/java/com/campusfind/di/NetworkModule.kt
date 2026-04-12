package com.campusfind.di

import com.campusfind.BuildConfig
import com.campusfind.data.remote.source.ClaimRemoteDataSource
import com.campusfind.data.remote.source.PhotoRemoteDataSource
import com.campusfind.data.remote.source.ClaimReplyRemoteDataSource
import com.campusfind.data.remote.source.LostItemRemoteDataSource
import com.campusfind.data.remote.source.TipRemoteDataSource
import com.campusfind.data.remote.source.UserRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Storage)
    }

    @Provides
    @Singleton
    fun provideUserRemoteDataSource(client: SupabaseClient) = UserRemoteDataSource(client)

    @Provides
    @Singleton
    fun provideLostItemRemoteDataSource(client: SupabaseClient) = LostItemRemoteDataSource(client)

    @Provides
    @Singleton
    fun provideClaimRemoteDataSource(client: SupabaseClient) = ClaimRemoteDataSource(client)

    @Provides
    @Singleton
    fun provideClaimReplyRemoteDataSource(client: SupabaseClient) = ClaimReplyRemoteDataSource(client)

    @Provides
    @Singleton
    fun provideTipRemoteDataSource(client: SupabaseClient) = TipRemoteDataSource(client)

    @Provides
    @Singleton
    fun providePhotoRemoteDataSource(client: SupabaseClient) = PhotoRemoteDataSource(client)
}