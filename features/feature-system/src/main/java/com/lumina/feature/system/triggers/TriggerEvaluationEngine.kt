package com.lumina.feature.system.triggers

import com.lumina.core.android.di.ApplicationScope
import com.lumina.core.common.time.TimeProvider
import com.lumina.core.logging.Logger
import com.lumina.core.model.LogicalOperator
import com.lumina.core.model.ProfileTriggerType
import com.lumina.core.model.SystemProfileIds
import com.lumina.domain.coordination.TriggerSystemStateCache
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.TriggerCondition
import com.lumina.feature.system.triggers.monitor.BluetoothTriggerMonitor
import com.lumina.feature.system.triggers.monitor.LocationTriggerMonitor
import com.lumina.feature.system.triggers.monitor.TimeTriggerMonitor
import com.lumina.feature.system.triggers.monitor.WifiTriggerMonitor
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
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
    private val timeProvider: TimeProvider,
    private val logger: Logger
) {
    private val TAG = this::class.java.simpleName
    private var collectionJob: Job? = null

    private var lastProfiledSwitchedAt: Long? = null
    private var noProfileMatchedTimestamp = 0L
    private val firstMatchTimestamps = mutableMapOf<String, Long>()

    @OptIn(FlowPreview::class)
    fun start() {
        if (collectionJob?.isActive == true) return

        collectionJob = scope.launch {
            merge(
                triggerSystemStateCache.currentSsid.map { Unit },
                triggerSystemStateCache.connectedDevices.map { Unit },
                triggerSystemStateCache.activeGeofenceIds.map { Unit }
            )
                .conflate()
                .debounce(TriggersConstant.TRIGGER_DEBOUNCE_MS)
                .collect { evaluateTriggers() }
        }
    }

    fun stop() {
        collectionJob?.cancel()
        collectionJob = null
    }

    suspend fun evaluateTriggers() {
        val activeProfile = profileRepository.activeProfile.first()
        if (activeProfile?.restrictions?.blockProfileTriggerSwitching == true) return

        val now = timeProvider.now()
        val lastSwitchTime = lastProfiledSwitchedAt
        if (lastSwitchTime != null && now - lastSwitchTime < TriggersConstant.PROFILE_SWITCH_COOLDOWN_MS) return

        val profiles = profileRepository.getAllProfiles().first()
            .sortedByDescending { it.priorityTriggerLaunch }

        var matchedProfileId: String? = null
        var anyProfileMatching = false

        for (profile in profiles) {
            if (profile.id == activeProfile?.id) continue

            val triggers = profileRepository.getProfileTriggers(profile.id).first()
            if (triggers.isEmpty()) continue

            val matches = evaluateSequence(triggers)
            if (!matches) {
                firstMatchTimestamps.remove(profile.id)
                continue
            }

            // Only switch to the profile if the triggers have met for asked amount of time.
            val firstMatchTime = firstMatchTimestamps.getOrPut(profile.id) { now }
            anyProfileMatching = true
            noProfileMatchedTimestamp = 0L

            val isStable = (now - firstMatchTime) >= TriggersConstant.TRIGGER_MATCHES_FOR_BEFORE_EXECUTING_MS
            if (!isStable) continue

            matchedProfileId = profile.id
            break
        }

        val targetProfileId = matchedProfileId ?: run {
            if (anyProfileMatching) return@run activeProfile?.id
            if (noProfileMatchedTimestamp == 0L) noProfileMatchedTimestamp = now

            val canResetToDefault = now -  noProfileMatchedTimestamp >= TriggersConstant.RESET_TO_DEFAULT_PROFILE_AFTER_MS
            if (canResetToDefault) {
                noProfileMatchedTimestamp = 0L
                SystemProfileIds.DEFAULT
            } else {
                activeProfile?.id
            }
        }

        if (targetProfileId == activeProfile?.id) return
        if (targetProfileId == null) return

        logger.d(TAG, "Triggers matched for profile: $targetProfileId")
        profileRepository.setActiveProfile(targetProfileId)

        lastProfiledSwitchedAt = now
        noProfileMatchedTimestamp = 0L
        firstMatchTimestamps.clear()

        return
    }

    private fun evaluateSequence(triggers: List<TriggerCondition>): Boolean {
        if (triggers.isEmpty()) return false

        val sorted = triggers.sortedBy { it.sequenceOrder }
        var result = evaluateSingle(sorted[0])
        if (sorted[0].stopIfTrue && result) return true

        for (trigger in sorted.drop(1)) {
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
