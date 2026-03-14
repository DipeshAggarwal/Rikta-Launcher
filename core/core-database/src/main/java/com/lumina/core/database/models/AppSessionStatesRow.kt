package com.lumina.core.database.models

data class AppSessionStatesRow(
    val totalMs: Long,
    val maxMs: Long,
    val sessionCount: Int
)
