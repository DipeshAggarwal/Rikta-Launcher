package com.lumina.data.profiles.di

import com.lumina.data.profiles.RoomProfileFavouriteRepository
import com.lumina.domain.profiles.ProfileFavouriteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileFavouriteRepositoryRoomModule {

    @Binds
    @Singleton
    abstract fun bindsProfileFavouriteRepository(
        profileFavouriteRepository: RoomProfileFavouriteRepository
    ): ProfileFavouriteRepository
}
