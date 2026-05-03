package com.lumina.feature.system.triggers

import com.lumina.core.logging.Logger
import com.lumina.core.model.LogicalOperator
import com.lumina.core.model.SystemProfileIds
import com.lumina.core.testing.builder.LauncherProfileBuilder
import com.lumina.core.testing.fake.FakeProfileRepository
import com.lumina.core.testing.fake.FakeTimeProvider
import com.lumina.feature.system.triggers.builder.TriggerConditionBuilder
import com.lumina.feature.system.triggers.monitor.BluetoothTriggerMonitor
import com.lumina.feature.system.triggers.monitor.LocationTriggerMonitor
import com.lumina.feature.system.triggers.monitor.TimeTriggerMonitor
import com.lumina.feature.system.triggers.monitor.TimeTriggerScheduler
import com.lumina.feature.system.triggers.monitor.WifiTriggerMonitor
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TriggerEvaluationEngineTest {
    private lateinit var fakeProfileRepository: FakeProfileRepository
    private lateinit var triggerSystemStateCache: TriggerSystemStateCache
    private lateinit var triggerEvaluationEngine: TriggerEvaluationEngine
    private lateinit var wifiTriggerMonitor: WifiTriggerMonitor
    private lateinit var bluetoothTriggerMonitor: BluetoothTriggerMonitor
    private lateinit var timeTriggerMonitor: TimeTriggerMonitor
    private lateinit var locationTriggerMonitor: LocationTriggerMonitor
    private lateinit var timeTriggerScheduler: TimeTriggerScheduler
    private lateinit var fakeTimeProvider: FakeTimeProvider
    private lateinit var logger: Logger

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val profileOne = LauncherProfileBuilder.build(
        id = "profile_test_1",
        name = "Test Profile One",
        priorityTriggerLaunch = false
    )

    private val profileTwo = LauncherProfileBuilder.build(
        id = "profile_test_2",
        name = "Test Profile Two",
        priorityTriggerLaunch = false
    )

    private val profileThree = LauncherProfileBuilder.build(
        id = "profile_test_3",
        name = "Test Profile Three",
        priorityTriggerLaunch = true
    )

    private suspend fun evaluateUntilStable() {
        triggerEvaluationEngine.evaluateTriggers()
        fakeTimeProvider.advanceBy(TriggersConstant.RESET_TO_DEFAULT_PROFILE_AFTER_MS)
        triggerEvaluationEngine.evaluateTriggers()
    }

    @Before
    fun setup() {
        fakeProfileRepository = FakeProfileRepository()
        triggerSystemStateCache = TriggerSystemStateCache()
        wifiTriggerMonitor = mockk(relaxed = true)
        bluetoothTriggerMonitor = mockk(relaxed = true)
        timeTriggerMonitor = mockk(relaxed = true)
        locationTriggerMonitor = mockk(relaxed = true)
        timeTriggerScheduler = mockk(relaxed = true)
        fakeTimeProvider = FakeTimeProvider(initialTime = 0L)

        triggerEvaluationEngine = TriggerEvaluationEngine(
            scope = testScope,
            profileRepository = fakeProfileRepository,
            triggerSystemStateCache = triggerSystemStateCache,
            wifiTriggerMonitor = wifiTriggerMonitor,
            bluetoothTriggerMonitor = bluetoothTriggerMonitor,
            timeTriggerMonitor = timeTriggerMonitor,
            locationTriggerMonitor = locationTriggerMonitor,
            timeProvider = fakeTimeProvider,
            logger = mockk(relaxed = true)
        )
    }

    @Test
    fun `wifi trigger match`() = runTest {
        val trigger = TriggerConditionBuilder.wifi()

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, trigger)
        every { wifiTriggerMonitor.evaluate(trigger) } returns true

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.contains(profileOne.id))
    }

    @Test
    fun `wifi trigger does not match`() = runTest {
        val trigger = TriggerConditionBuilder.wifi()

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, trigger)
        every { wifiTriggerMonitor.evaluate(trigger) } returns false

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.isEmpty())
    }

    @Test
    fun `bluetooth trigger match`() = runTest {
        val trigger = TriggerConditionBuilder.bluetooth()

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, trigger)
        every { bluetoothTriggerMonitor.evaluate(trigger) } returns true

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.contains(profileOne.id))
    }

    @Test
    fun `bluetooth trigger does not match`() = runTest {
        val trigger = TriggerConditionBuilder.bluetooth()

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, trigger)
        every { bluetoothTriggerMonitor.evaluate(trigger) } returns false

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.isEmpty())
    }

    @Test
    fun `location trigger match`() = runTest {
        val trigger = TriggerConditionBuilder.location()

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, trigger)
        every { locationTriggerMonitor.evaluate(trigger) } returns true

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.contains(profileOne.id))
    }

    @Test
    fun `location trigger does not match`() = runTest {
        val trigger = TriggerConditionBuilder.location()

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, trigger)
        every { locationTriggerMonitor.evaluate(trigger) } returns false

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.isEmpty())
    }

    @Test
    fun `profile with no trigger is skipped`() = runTest {
        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id)

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.isEmpty())
    }

    @Test
    fun `no switch when no profiles are added`() = runTest {
        fakeProfileRepository.setProfiles()

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.isEmpty())
    }

    @Test
    fun `AND condition triggers match when both are true`() = runTest {
        val triggerOne = TriggerConditionBuilder.wifi(logicalOperator = null)
        val triggerTwo = TriggerConditionBuilder.bluetooth(logicalOperator = LogicalOperator.AND)

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, triggerOne, triggerTwo)

        every { wifiTriggerMonitor.evaluate(triggerOne) } returns true
        every { bluetoothTriggerMonitor.evaluate(triggerTwo) } returns true

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.contains(profileOne.id))
    }

    @Test
    fun `AND condition triggers do not match when only first is true`() = runTest {
        val triggerOne = TriggerConditionBuilder.wifi(logicalOperator = null)
        val triggerTwo = TriggerConditionBuilder.bluetooth(logicalOperator = LogicalOperator.AND)

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, triggerOne, triggerTwo)

        every { wifiTriggerMonitor.evaluate(triggerOne) } returns true
        every { bluetoothTriggerMonitor.evaluate(triggerTwo) } returns false

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.isEmpty())
    }

    @Test
    fun `AND condition triggers do not match when only second is true`() = runTest {
        val triggerOne = TriggerConditionBuilder.wifi(logicalOperator = null)
        val triggerTwo = TriggerConditionBuilder.bluetooth(logicalOperator = LogicalOperator.AND)

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, triggerOne, triggerTwo)

        every { wifiTriggerMonitor.evaluate(triggerOne) } returns false
        every { bluetoothTriggerMonitor.evaluate(triggerTwo) } returns true

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.isEmpty())
    }

    @Test
    fun `AND condition triggers do not match when both are false`() = runTest {
        val triggerOne = TriggerConditionBuilder.wifi(logicalOperator = null)
        val triggerTwo = TriggerConditionBuilder.bluetooth(logicalOperator = LogicalOperator.AND)

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, triggerOne, triggerTwo)

        every { wifiTriggerMonitor.evaluate(triggerOne) } returns false
        every { bluetoothTriggerMonitor.evaluate(triggerTwo) } returns false

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.isEmpty())
    }

    @Test
    fun `OR condition triggers match when first is true`() = runTest {
        val triggerOne = TriggerConditionBuilder.wifi(logicalOperator = null)
        val triggerTwo = TriggerConditionBuilder.bluetooth(logicalOperator = LogicalOperator.OR)

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, triggerOne, triggerTwo)

        every { wifiTriggerMonitor.evaluate(triggerOne) } returns true
        every { bluetoothTriggerMonitor.evaluate(triggerTwo) } returns false

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.contains(profileOne.id))
    }

    @Test
    fun `OR condition triggers match when second is true`() = runTest {
        val triggerOne = TriggerConditionBuilder.wifi(logicalOperator = null)
        val triggerTwo = TriggerConditionBuilder.bluetooth(logicalOperator = LogicalOperator.OR)

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, triggerOne, triggerTwo)

        every { wifiTriggerMonitor.evaluate(triggerOne) } returns false
        every { bluetoothTriggerMonitor.evaluate(triggerTwo) } returns true

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.contains(profileOne.id))
    }

    @Test
    fun `OR condition triggers do not match when both are false`() = runTest {
        val triggerOne = TriggerConditionBuilder.wifi(logicalOperator = null)
        val triggerTwo = TriggerConditionBuilder.bluetooth(logicalOperator = LogicalOperator.OR)

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, triggerOne, triggerTwo)

        every { wifiTriggerMonitor.evaluate(triggerOne) } returns false
        every { bluetoothTriggerMonitor.evaluate(triggerTwo) } returns false

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.isEmpty())
    }

    @Test
    fun `NOT condition triggers when first is true and second is false`() = runTest {
        val triggerOne = TriggerConditionBuilder.wifi(logicalOperator = null)
        val triggerTwo = TriggerConditionBuilder.bluetooth(logicalOperator = LogicalOperator.NOT)

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, triggerOne, triggerTwo)

        every { wifiTriggerMonitor.evaluate(triggerOne) } returns true
        every { bluetoothTriggerMonitor.evaluate(triggerTwo) } returns false

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.contains(profileOne.id))
    }

    @Test
    fun `NOT condition triggers when both are true`() = runTest {
        val triggerOne = TriggerConditionBuilder.wifi(logicalOperator = null)
        val triggerTwo = TriggerConditionBuilder.bluetooth(logicalOperator = LogicalOperator.NOT)

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, triggerOne, triggerTwo)

        every { wifiTriggerMonitor.evaluate(triggerOne) } returns true
        every { bluetoothTriggerMonitor.evaluate(triggerTwo) } returns true

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.isEmpty())
    }

    @Test
    fun `trigger evaluation return true if matched and stopIfTrue == true`() = runTest {
        val triggerOne = TriggerConditionBuilder.wifi(logicalOperator = null, stopIfTrue = true)
        val triggerTwo = TriggerConditionBuilder.bluetooth(logicalOperator = LogicalOperator.AND)

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, triggerOne, triggerTwo)

        every { wifiTriggerMonitor.evaluate(triggerOne) } returns true
        evaluateUntilStable()

        assert(fakeProfileRepository.setActiveProfileCalls.contains(profileOne.id))
        verify(exactly = 0) { bluetoothTriggerMonitor.evaluate(any()) }
    }

    @Test
    fun `trigger evaluation continues if first trigger does not match and stopIfTrue == true`() = runTest {
        val triggerOne = TriggerConditionBuilder.wifi(logicalOperator = null, stopIfTrue = true)
        val triggerTwo = TriggerConditionBuilder.bluetooth(logicalOperator = LogicalOperator.OR)

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, triggerOne, triggerTwo)

        every { wifiTriggerMonitor.evaluate(triggerOne) } returns false
        every { bluetoothTriggerMonitor.evaluate(triggerTwo) } returns true

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.contains(profileOne.id))
    }

    @Test
    fun `priority profile are evaluated first`() = runTest {
        val priorityTrigger = TriggerConditionBuilder.wifi(wifiSsid = "RiktaWifi")
        val normalTrigger = TriggerConditionBuilder.wifi(wifiSsid = "HomeWifi")

        fakeProfileRepository.setProfiles(profileOne, profileThree)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, normalTrigger)
        fakeProfileRepository.setTriggersForProfile(profileThree.id, priorityTrigger)

        every { wifiTriggerMonitor.evaluate(normalTrigger) } returns true
        every { wifiTriggerMonitor.evaluate(priorityTrigger) } returns true

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.first() == profileThree.id)
    }

    @Test
    fun `blockProfileTriggerSwitching prevents profile switching`() = runTest {
        val trigger = TriggerConditionBuilder.wifi()
        val blockingProfile = LauncherProfileBuilder.build(
            id = "profile_blocking",
            name = "You shall not Pass!",
            blockProfileTriggerSwitching = true
        )

        fakeProfileRepository.setProfiles(blockingProfile, profileOne)
        fakeProfileRepository.setActiveProfile(blockingProfile.id)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, trigger)
        every { wifiTriggerMonitor.evaluate(trigger) } returns true

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.size == 1)
        assert(fakeProfileRepository.setActiveProfileCalls.last() == blockingProfile.id)
    }

    @Test
    fun `first matching profile returns and evaluation is stopped`() = runTest {
        val triggerOne = TriggerConditionBuilder.wifi(wifiSsid = "RiktaWifi")
        val triggerTwo = TriggerConditionBuilder.wifi(wifiSsid = "HomeWifi")

        fakeProfileRepository.setProfiles(profileOne, profileTwo)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, triggerOne)
        fakeProfileRepository.setTriggersForProfile(profileTwo.id, triggerTwo)

        every { wifiTriggerMonitor.evaluate(triggerOne) } returns true
        every { wifiTriggerMonitor.evaluate(triggerTwo) } returns true

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.size == 1)
    }

    @Test
    fun `no profile matches`() = runTest {
        val trigger = TriggerConditionBuilder.wifi()

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, trigger)
        every { wifiTriggerMonitor.evaluate(trigger) } returns false

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.isEmpty())
    }

    @Test
    fun `no profile switch happens until it is stable`() = runTest {
        val trigger = TriggerConditionBuilder.wifi()

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, trigger)
        every { wifiTriggerMonitor.evaluate(trigger) } returns true

        triggerEvaluationEngine.evaluateTriggers()
        assert(fakeProfileRepository.setActiveProfileCalls.isEmpty())

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.contains(profileOne.id))
    }

    @Test
    fun `resets to default profile after no match for given time`() = runTest {
        val trigger = TriggerConditionBuilder.wifi()

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, trigger)
        every { wifiTriggerMonitor.evaluate(trigger) } returns false

        triggerEvaluationEngine.evaluateTriggers()
        assert(fakeProfileRepository.setActiveProfileCalls.isEmpty())

        // Advance past cooldown - sets noProfileMatchedTimestamp.
        fakeTimeProvider.advanceBy(TriggersConstant.PROFILE_SWITCH_COOLDOWN_MS)
        triggerEvaluationEngine.evaluateTriggers()
        assert(fakeProfileRepository.setActiveProfileCalls.isEmpty())

        // Advance past Reset window and the third evaluates, executes perfectly.
        fakeTimeProvider.advanceBy(TriggersConstant.RESET_TO_DEFAULT_PROFILE_AFTER_MS)
        triggerEvaluationEngine.evaluateTriggers()
        assert(fakeProfileRepository.setActiveProfileCalls.contains(SystemProfileIds.DEFAULT))
    }

    @Test
    fun `does not reset to default if a profile matches mid cooldown for default profile`() = runTest {
        val trigger = TriggerConditionBuilder.wifi()

        fakeProfileRepository.setProfiles(profileOne)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, trigger)
        every { wifiTriggerMonitor.evaluate(trigger) } returns false

        // Set noProfileMatchedTimestamp.
        fakeTimeProvider.advanceBy(TriggersConstant.PROFILE_SWITCH_COOLDOWN_MS)
        triggerEvaluationEngine.evaluateTriggers()
        assert(fakeProfileRepository.setActiveProfileCalls.isEmpty())

        // Match profile trigger. Set noProfileMatchedTimestamp = 0.
        every { wifiTriggerMonitor.evaluate(trigger) } returns true
        fakeTimeProvider.advanceBy(TriggersConstant.RESET_TO_DEFAULT_PROFILE_AFTER_MS)
        triggerEvaluationEngine.evaluateTriggers()

        // Advance past the reset window.
        fakeTimeProvider.advanceBy(TriggersConstant.TRIGGER_MATCHES_FOR_BEFORE_EXECUTING_MS)
        triggerEvaluationEngine.evaluateTriggers()
        assert(fakeProfileRepository.setActiveProfileCalls.contains(profileOne.id))
    }

    @Test
    fun `profile can only switch after cooldown time has passed`() = runTest {
        val triggerOne = TriggerConditionBuilder.wifi(wifiSsid = "RiktaWifi")
        val triggerTwo = TriggerConditionBuilder.wifi(wifiSsid = "HomeWifi")

        fakeProfileRepository.setProfiles(profileOne, profileTwo)
        fakeProfileRepository.setTriggersForProfile(profileOne.id, triggerOne)
        fakeProfileRepository.setTriggersForProfile(profileTwo.id, triggerTwo)

        every { wifiTriggerMonitor.evaluate(triggerOne) } returns true
        every { wifiTriggerMonitor.evaluate(triggerTwo) } returns false

        evaluateUntilStable()
        assert(fakeProfileRepository.setActiveProfileCalls.contains(profileOne.id))

        every { wifiTriggerMonitor.evaluate(triggerOne) } returns false
        every { wifiTriggerMonitor.evaluate(triggerTwo) } returns true

        // In cooldown. firstMatchTimestamp is not set.
        fakeTimeProvider.advanceBy(TriggersConstant.PROFILE_SWITCH_COOLDOWN_MS - 1)
        triggerEvaluationEngine.evaluateTriggers()
        assert(fakeProfileRepository.setActiveProfileCalls.contains(profileOne.id))

        // Still in cooldown. Set firstMatchTimestamp.
        fakeTimeProvider.advanceBy(2)
        triggerEvaluationEngine.evaluateTriggers()

        // Cooldown ends. Profile switches.
        fakeTimeProvider.advanceBy(TriggersConstant.TRIGGER_MATCHES_FOR_BEFORE_EXECUTING_MS)
        triggerEvaluationEngine.evaluateTriggers()
        assert(fakeProfileRepository.setActiveProfileCalls.contains(profileTwo.id))
    }
}
