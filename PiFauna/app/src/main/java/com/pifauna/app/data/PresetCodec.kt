package com.pifauna.app.data

import com.pifauna.app.audio.ToneMode
import com.pifauna.app.safety.SafetyLimits

/** A user-saved frequency configuration. Local only; never leaves the device. */
data class Preset(
    val name: String,
    val startHz: Int,
    val endHz: Int,
    val mode: ToneMode,
    val gain: Float
)

/** Raised when a preset cannot be accepted; the message is shown to the user. */
class PresetRejected(message: String) : IllegalArgumentException(message)

/**
 * Line-oriented codec for [Preset] lists.
 *
 * Pure Kotlin so the storage format is unit-tested without Android. Fields are
 * joined with the ASCII unit separator and records with the record separator;
 * both characters are rejected from names, which removes the escaping problem
 * instead of trying to solve it.
 */
object PresetCodec {

    private const val FIELD = '\u001F'
    private const val RECORD = '\u001E'
    private const val FORMAT_VERSION = "1"

    const val MAX_NAME_LENGTH = 24

    fun validateName(raw: String): String {
        val name = raw.trim()
        if (name.isEmpty()) throw PresetRejected("Enter a name for the preset.")
        if (name.length > MAX_NAME_LENGTH) {
            throw PresetRejected("Names are limited to $MAX_NAME_LENGTH characters.")
        }
        if (name.any { it == FIELD || it == RECORD || it.isISOControl() }) {
            throw PresetRejected("That name contains an unsupported character.")
        }
        return name
    }

    fun validateRange(startHz: Int, endHz: Int) {
        if (startHz !in SafetyLimits.MIN_HZ..SafetyLimits.MAX_HZ ||
            endHz !in SafetyLimits.MIN_HZ..SafetyLimits.MAX_HZ
        ) {
            throw PresetRejected(
                "Frequencies must be between ${SafetyLimits.MIN_HZ} and ${SafetyLimits.MAX_HZ} Hz."
            )
        }
        if (startHz > endHz) throw PresetRejected("The start frequency must not exceed the end.")
    }

    /** Adds [preset] to [existing], enforcing the name rules and the storage cap. */
    fun add(existing: List<Preset>, preset: Preset): List<Preset> {
        val name = validateName(preset.name)
        validateRange(preset.startHz, preset.endHz)
        if (preset.gain > SafetyLimits.MAX_GAIN) {
            throw PresetRejected("Gain above ${SafetyLimits.MAX_GAIN} is not allowed.")
        }
        if (existing.size >= SafetyLimits.MAX_PRESETS) {
            throw PresetRejected("Only ${SafetyLimits.MAX_PRESETS} presets can be stored. Delete one first.")
        }
        if (existing.any { it.name.equals(name, ignoreCase = true) }) {
            throw PresetRejected("A preset named \"$name\" already exists.")
        }
        return existing + preset.copy(name = name)
    }

    fun encode(presets: List<Preset>): String = buildString {
        append(FORMAT_VERSION)
        for (preset in presets) {
            append(RECORD)
            append(preset.name)
            append(FIELD).append(preset.startHz)
            append(FIELD).append(preset.endHz)
            append(FIELD).append(preset.mode.name)
            append(FIELD).append(preset.gain)
        }
    }

    fun decode(raw: String?): List<Preset> {
        if (raw.isNullOrBlank()) return emptyList()
        val records = raw.split(RECORD)
        if (records.firstOrNull() != FORMAT_VERSION) return emptyList()

        return records.drop(1).mapNotNull { record ->
            val parts = record.split(FIELD)
            if (parts.size != 5) return@mapNotNull null
            val start = parts[1].toIntOrNull() ?: return@mapNotNull null
            val end = parts[2].toIntOrNull() ?: return@mapNotNull null
            val mode = runCatching { ToneMode.valueOf(parts[3]) }.getOrNull() ?: return@mapNotNull null
            val gain = parts[4].toFloatOrNull() ?: return@mapNotNull null
            if (start !in SafetyLimits.MIN_HZ..SafetyLimits.MAX_HZ) return@mapNotNull null
            if (end !in SafetyLimits.MIN_HZ..SafetyLimits.MAX_HZ) return@mapNotNull null
            if (gain <= 0f || gain > SafetyLimits.MAX_GAIN) return@mapNotNull null
            Preset(parts[0], start, end, mode, gain)
        }
    }
}
