package com.lumina.feature.system.triggers

import android.Manifest
import androidx.annotation.RequiresPermission
import com.lumina.core.android.di.ApplicationScope
import com.lumina.core.logging.Logger
import com.lumina.core.model.LogicalOperator
import com.lumina.core.model.ProfileTriggerType
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.TriggerCondition
import com.lumina.feature.system.triggers.monitor.BluetoothTriggerMonitor
import com.lumina.feature.system.triggers.monitor.LocationTriggerMonitor
import com.lumina.feature.system.triggers.monitor.TimeTriggerMonitor
import com.lumina.feature.system.triggers.monitor.TimeTriggerScheduler
import com.lumina.feature.system.triggers.monitor.WifiTriggerMonitor
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

@Singleton
class TriggerEvaluationEngine @Inject constructor(
    @param:ApplicationScope private val scope: CoroutineScope,
    private val profileRepository: ProfileRepository,
    private val triggerSystemStateCache: TriggerSystemStateCache,
    private val wifiTriggerMonitor: WifiTriggerMonitor,
    private val bluetoothTriggerMonitor: BluetoothTriggerMonitor,
    private val timeTriggerMonitor: TimeTriggerMonitor,
    private val locationTriggerMonitor: LocationTriggerMonitor,
    private val logger: Logger
) {
    private val TAG = this::class.java.simpleName
    private var collectionJob: Job? = null

    fun start() {
        if (collectionJob?.isActive == true) return

        collectionJob = scope.launch {
            merge(
                triggerSystemStateCache.currentSsid.map { Unit },
                triggerSystemStateCache.connectedDevices.map { Unit },
                triggerSystemStateCache.activeGeofenceIds.map { Unit }
            ).collect { evaluateTriggers() }
        }
    }

    fun stop() {
        collectionJob?.cancel()
        collectionJob = null
    }

    suspend fun evaluateTriggers() {
        val profiles = profileRepository.getAllProfiles().first()
            .sortedByDescending { it.priorityTriggerLaunch }

        for (profile in profiles) {
            val triggers = profileRepository.getProfileTriggers(profile.id).first()
            if (triggers.isEmpty()) continue

            if (evaluateSequence(triggers)) {
                logger.d(TAG, "Triggers matched for profile: ${profile.name}")
                profileRepository.setActiveProfile(profile.id)

                return
            }
        }
    }

    private fun evaluateSequence(triggers: List<TriggerCondition>): Boolean {
        val sorted = triggers.sortedBy { it.sequenceOrder }
        var result = false

        for (trigger in sorted) {
            val match = evaluateSingle(trigger)
            if (trigger.stopIfTrue && match) return true

            result = when (trigger.logicalOperator) {
                LogicalOperator.AND -> result && match
                LogicalOperator.OR -> result || match
                LogicalOperator.NOT -> result && !match
                null -> match
            }
        }

        return result
    }

    private fun evaluateSingle(trigger: TriggerCondition): Boolean {
        return when (trigger.triggerType) {
            ProfileTriggerType.TIME -> timeTriggerMonitor.evaluate(trigger)
            ProfileTriggerType.DAY -> timeTriggerMonitor.evaluate(trigger)
            ProfileTriggerType.LOCATION -> locationTriggerMonitor.evaluate(trigger)
            ProfileTriggerType.WIFI -> wifiTriggerMonitor.evaluate(trigger)
            ProfileTriggerType.BLUETOOTH -> bluetoothTriggerMonitor.evaluate(trigger)
            null -> false
        }
    }
}
