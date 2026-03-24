package com.lumina.core.model

fun getAppKey(packageName: String, userHandleNumber: Long): String = "$packageName::$userHandleNumber"

val AppInfo.componentKey: String
    get() = getAppKey(packageName, userHandleNumber)

val AppBasicData.componentKey: String
    get() = getAppKey(packageName, userHandleNumber)

val AppBasicData.componentData: Pair<String, Long>
    get() = packageName to userHandleNumber

fun List<AppInfo>.toAppMap(): Map<String, AppInfo> = associateBy { it.componentKey }
