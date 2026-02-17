package com.lumina.core.common

import jakarta.inject.Qualifier

/**
 * Qualifier for the Default dispatcher, used for CPU-intensive work
 * (e.g., sorting large lists, complex search algorithms).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

/**
 * Qualifier for the IO dispatcher, used for disk or network operations
 * (e.g., DataStore access, Package Manager IPC calls).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * Qualifier for the Main dispatcher, used for UI interactions and updating StateFlows.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher
