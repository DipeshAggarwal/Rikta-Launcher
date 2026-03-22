package com.lumina.core.common.time

import jakarta.inject.Inject
import jakarta.inject.Singleton

interface TimeProvider {
    fun now(): Long
}

@Singleton
class SystemTimeProvider @Inject constructor() : TimeProvider {
    override fun now(): Long = System.currentTimeMillis()
}
