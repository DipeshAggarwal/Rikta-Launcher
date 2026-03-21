package com.lumina.data.profiles.di

import com.lumina.data.profiles.ProfileDataStore
import com.lumina.data.profiles.ProfileDataStoreRepository
import com.lumina.data.profiles.RoomProfileRepository
import com.lumina.domain.profiles.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileRepositoryRoomModule {

    @Binds
    @Singleton
    abstract fun bindsProfileRepository(
        profileRepository: RoomProfileRepository
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindsProfileDataStore(profileDataStore: ProfileDataStoreRepository) : ProfileDataStore
}
