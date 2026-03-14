package com.lumina.data.shortcut.di

import com.lumina.data.shortcut.RoomShortcutRepository
import com.lumina.domain.shortcut.ShortcutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ShortcutRepositoryRoomModule {

    @Binds
    @Singleton
    abstract fun bindsShortcutRepository(
        shortcutRepository: RoomShortcutRepository
    ): ShortcutRepository
}