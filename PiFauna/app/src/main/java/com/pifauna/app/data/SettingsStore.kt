package com.pifauna.app.data

import android.content.Context
import com.pifauna.app.audio.ToneMode
import com.pifauna.app.safety.SafetyLimits

/**
 * Tiny local settings store.
 *
 * SharedPreferences only: no account, no network, no analytics. Every value is
 * clamped on read so a corrupted file cannot widen a safety limit.
 */
class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    var lastProfileId: String
        get() = prefs.getString(KEY_PROFILE, null) ?: SpeciesProfiles.all.first().id
        set(value) = prefs.edit().putString(KEY_PROFILE, value).apply()

    var lastFrequencyHz: Int
        get() = prefs.getInt(KEY_FREQUENCY, -1)
            .takeIf { it in SafetyLimits.MIN_HZ..SafetyLimits.MAX_HZ }
            ?: SpeciesProfiles.all.first().lastHz
        set(value) = prefs.edit()
            .putInt(KEY_FREQUENCY, value.coerceIn(SafetyLimits.MIN_HZ, SafetyLimits.MAX_HZ))
            .apply()

    var lastDurationMinutes: Int
        get() = prefs.getInt(KEY_DURATION, 10)
            .takeIf { it in SafetyLimits.DURATION_CHOICES_MIN }
            ?: 10
        set(value) = prefs.edit()
            .putInt(KEY_DURATION, value.takeIf { it in SafetyLimits.DURATION_CHOICES_MIN } ?: 10)
            .apply()

    var lastMode: ToneMode
        get() = prefs.getString(KEY_MODE, null)
            ?.let { runCatching { ToneMode.valueOf(it) }.getOrNull() }
            ?: ToneMode.FIXED
        set(value) = prefs.edit().putString(KEY_MODE, value.name).apply()

    var lastGain: Float
        get() = prefs.getFloat(KEY_GAIN, DEFAULT_GAIN)
            .takeIf { it > 0f && it <= SafetyLimits.MAX_GAIN }
            ?: DEFAULT_GAIN
        set(value) = prefs.edit()
            .putFloat(KEY_GAIN, value.coerceIn(0.01f, SafetyLimits.MAX_GAIN))
            .apply()

    var welcomeSeen: Boolean
        get() = prefs.getBoolean(KEY_WELCOME, false)
        set(value) = prefs.edit().putBoolean(KEY_WELCOME, value).apply()

    /** Explicit opt-in, OFF by default. Never auto-starts audio. */
    var continueWhenScreenLocked: Boolean
        get() = prefs.getBoolean(KEY_SCREEN_LOCK_PLAYBACK, false)
        set(value) = prefs.edit().putBoolean(KEY_SCREEN_LOCK_PLAYBACK, value).apply()

    var playbackSafetyAcknowledged: Boolean
        get() = prefs.getBoolean(KEY_SAFETY_ACK, false)
        set(value) = prefs.edit().putBoolean(KEY_SAFETY_ACK, value).apply()

    /** Nothing here is sensitive enough to leave the device, but clear it on request. */
    fun clearAll() = prefs.edit().clear().apply()

    companion object {
        const val DEFAULT_GAIN = 0.05f

        private const val FILE = "pifauna_settings"
        private const val KEY_PROFILE = "last_profile"
        private const val KEY_FREQUENCY = "last_frequency_hz"
        private const val KEY_DURATION = "last_duration_min"
        private const val KEY_MODE = "last_mode"
        private const val KEY_GAIN = "last_gain"
        private const val KEY_WELCOME = "welcome_seen"
        private const val KEY_SAFETY_ACK = "safety_acknowledged"
        private const val KEY_SCREEN_LOCK_PLAYBACK = "screen_lock_playback_opt_in"
    }
}

/** Persisted custom presets, encoded by [PresetCodec]. */
class PresetStore(context: Context) {

    private val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun load(): List<Preset> = PresetCodec.decode(prefs.getString(KEY, null))

    fun save(presets: List<Preset>) {
        prefs.edit().putString(KEY, PresetCodec.encode(presets)).apply()
    }

    fun add(preset: Preset): List<Preset> {
        val updated = PresetCodec.add(load(), preset)
        save(updated)
        return updated
    }

    fun remove(name: String): List<Preset> {
        val updated = load().filterNot { it.name.equals(name, ignoreCase = true) }
        save(updated)
        return updated
    }

    private companion object {
        const val FILE = "pifauna_presets"
        const val KEY = "presets"
    }
}
