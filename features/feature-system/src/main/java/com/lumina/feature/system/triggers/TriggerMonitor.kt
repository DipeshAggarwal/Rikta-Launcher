package com.lumina.feature.system.triggers

import com.lumina.domain.profiles.model.TriggerCondition

interface TriggerMonitor {
    fun evaluate(trigger: TriggerCondition): Boolean
}
