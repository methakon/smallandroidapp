package com.pifauna.app.safety

import com.pifauna.app.audio.OutputMode
import com.pifauna.app.audio.RoutedOutput
import org.junit.Assert.assertEquals
import org.junit.Test

class ExternalAudioConsentTest {
    private fun snapshot(
        mode: OutputMode = OutputMode.PHONE_SPEAKER,
        routed: RoutedOutput? = null,
        consent: Boolean = false,
        duration: Int = 5,
        gain: Float = 0.02f,
        volume: Float = 0.25f,
        available: String? = "Bluetooth A2DP available"
    ): SafetySnapshot = SafetySnapshot(
        batteryPercent = 90,
        thermalStatus = 0,
        mediaVolumeRatio = volume,
        availableExternalRoute = available,
        activeOutput = routed,
        outputMode = mode,
        externalSpeakerConsented = consent,
        selectedDurationMinutes = duration,
        selectedGain = gain
    )

    private fun reason(s: SafetySnapshot): BlockCode =
        (SafetyPolicy.evaluate(s) as SafetyDecision.Blocked).code

    @Test fun unrelatedBluetoothConnectionDoesNotBlockPhoneRoute() {
        assertEquals(SafetyDecision.Allowed,
            SafetyPolicy.evaluate(snapshot(routed = RoutedOutput.PHONE_SPEAKER)))
    }

    @Test fun phoneSelectedButActualAudioRoutedToA2dpIsBlocked() {
        assertEquals(BlockCode.EXTERNAL_ROUTE,
            reason(snapshot(routed = RoutedOutput.BLUETOOTH_A2DP_UNVERIFIED)))
    }

    @Test fun externalSpeakerNeedsNewSessionConsent() {
        assertEquals(BlockCode.EXTERNAL_CONSENT_REQUIRED,
            reason(snapshot(mode = OutputMode.EXTERNAL_BLUETOOTH_SPEAKER)))
    }

    @Test fun acknowledgedSpeakerCannotBypassHeadphonesOrUnknownRoutes() {
        for (route in listOf(RoutedOutput.HEADPHONES,
            RoutedOutput.HEARING_AID, RoutedOutput.UNKNOWN, RoutedOutput.OTHER_EXTERNAL)) {
            assertEquals(BlockCode.EXTERNAL_ROUTE,
                reason(snapshot(mode = OutputMode.EXTERNAL_BLUETOOTH_SPEAKER,
                    consent = true, routed = route)))
        }
    }

    @Test fun acknowledgedA2dpRequiresStrictTimingAndGain() {
        val approved = snapshot(mode = OutputMode.EXTERNAL_BLUETOOTH_SPEAKER,
            consent = true, routed = RoutedOutput.BLUETOOTH_A2DP_UNVERIFIED)
        assertEquals(SafetyDecision.Allowed, SafetyPolicy.evaluate(approved))
        assertEquals(BlockCode.INVALID_REQUEST, reason(approved.copy(selectedDurationMinutes = 10)))
        assertEquals(BlockCode.INVALID_REQUEST, reason(approved.copy(selectedGain = 0.04f)))
        assertEquals(BlockCode.VOLUME_TOO_HIGH, reason(approved.copy(mediaVolumeRatio = 0.31f)))
    }

    @Test fun externalSpeakerStillObeysBatteryAndThermal() {
        val approved = snapshot(mode = OutputMode.EXTERNAL_BLUETOOTH_SPEAKER,
            consent = true, routed = RoutedOutput.BLUETOOTH_LE_SPEAKER)
        assertEquals(BlockCode.LOW_BATTERY, reason(approved.copy(batteryPercent = 15)))
        assertEquals(BlockCode.THERMAL, reason(approved.copy(thermalStatus = 3)))
    }
}
