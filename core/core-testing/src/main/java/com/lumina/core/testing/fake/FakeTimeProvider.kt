package com.lumina.core.testing.fake

import com.lumina.core.common.time.TimeProvider

class FakeTimeProvider(initialTime: Long = 0L) : TimeProvider {
    var currentTime: Long = initialTime
        private set

    override fun now(): Long = currentTime

    fun advanceBy(ms: Long) {
        currentTime += ms
    }

    fun setTime(ms: Long) {
        currentTime = ms
    }
}
