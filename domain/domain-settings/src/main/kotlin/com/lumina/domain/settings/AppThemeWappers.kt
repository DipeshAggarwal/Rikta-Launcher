package com.lumina.domain.settings

import com.lumina.core.common.AppTheme

/**
 * Extension function to convert a domain-level [AppTheme] into a string suitable for persistent storage.
 * Using the enum name ensures a consistent, human-readable key.
 */
fun AppTheme.toStorageValue(): String = name
