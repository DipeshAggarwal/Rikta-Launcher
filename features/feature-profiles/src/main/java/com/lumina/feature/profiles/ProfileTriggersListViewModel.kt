package com.lumina.feature.profiles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.core.common.FlowDefaults.WhileSubscribedTimeoutMs
import com.lumina.core.logging.Logger
import com.lumina.core.model.LogicalOperator
import com.lumina.domain.coordination.TriggerScheduler
import com.lumina.domain.profiles.ProfileRepository
import com.lumina.domain.profiles.model.TriggerCondition
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProfileTriggersListEvent {
    data class NavigateToEditTrigger(val triggerId: Long) : ProfileTriggersListEvent
}

@HiltViewModel
class ProfileTriggersListViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val triggerScheduler: TriggerScheduler,
    savedStateHandle: SavedStateHandle,
    private val logger: Logger
) : ViewModel() {
    private val TAG = this::class.java.simpleName

    private val targetProfileId: String = checkNotNull(savedStateHandle[ProfileNavigationRoute.PROFILE_ID_ARG])

    val triggers: StateFlow<List<TriggerCondition>> = profileRepository
        .getProfileTriggers(targetProfileId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(WhileSubscribedTimeoutMs), emptyList())

    private val _errorMessage = MutableSharedFlow<String>(
        replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _events = MutableSharedFlow<ProfileTriggersListEvent>(
        replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<ProfileTriggersListEvent> = _events.asSharedFlow()

    private fun reorder(prev: TriggerCondition, next: TriggerCondition) {
        if (prev.sequenceOrder == next.sequenceOrder) {
            logger.e(TAG, "sequenceOrder for ${prev.triggerId} and ${next.triggerId}.")
            _errorMessage.tryEmit("Couldn't reorder. Please try again.")
            return
        }

        val movedPrev = prev.copy(sequenceOrder = next.sequenceOrder, logicalOperator = next.logicalOperator)
        val movedNext = next.copy(sequenceOrder = prev.sequenceOrder, logicalOperator = prev.logicalOperator)

        viewModelScope.launch {
            try {
                profileRepository.swapTriggerOrder(movedPrev, movedNext)
                triggerScheduler.refresh()
            } catch (e: Exception) {
                logger.e(TAG, "Trigger reorder failed.", e)
                _errorMessage.tryEmit("Couldn't reorder. Please try again.")
            }
        }
    }

    fun onEditTrigger(triggerID: Long) {
        viewModelScope.launch {
            _events.emit(ProfileTriggersListEvent.NavigateToEditTrigger(triggerID))
        }
    }

    fun onDuplicate(trigger: TriggerCondition) {
        viewModelScope.launch {
            try {
                profileRepository.addProfileTrigger(
                    targetProfileId,
                    trigger.copy(triggerId = 0L, logicalOperator = LogicalOperator.AND, stopIfTrue = false)
                )
                triggerScheduler.refresh()
            } catch (e: Exception) {
                logger.e(TAG, "Trigger duplication failed for ${trigger.triggerId}.", e)
                _errorMessage.tryEmit("Duplication failed. Please try again.")
            }
        }
    }

    fun onDelete(triggerId: Long) {
        viewModelScope.launch {
            try {
                profileRepository.removeProfileTrigger(triggerId)
                triggerScheduler.refresh()
            } catch (e: Exception) {
                logger.e(TAG, "Trigger delete failed for $triggerId", e)
                _errorMessage.tryEmit("Deletion failed. Please try again.")
            }
        }
    }

    fun updateOperator(trigger: TriggerCondition, operator: LogicalOperator) {
        viewModelScope.launch {
            try {
                profileRepository.updateProfileTrigger(trigger.copy(logicalOperator = operator))
                triggerScheduler.refresh()
            } catch (e: Exception) {
                logger.e(TAG, "Failed to update operator for trigger ${trigger.triggerId}.", e)
                _errorMessage.tryEmit("Failed to update trigger.")
            }
        }
    }

    fun moveUp(index: Int) {
        if (index <= 0 || index >= triggers.value.size) return
        reorder(triggers.value[index], triggers.value[index - 1])
    }

    fun moveDown(index: Int) {
        if (index < 0 || index >= triggers.value.lastIndex) return
        reorder(triggers.value[index], triggers.value[index + 1])
    }
}
