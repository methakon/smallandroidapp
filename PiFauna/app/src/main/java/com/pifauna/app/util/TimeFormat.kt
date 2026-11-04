package com.pifauna.app.util

import java.util.Locale

/** Pure formatting helpers, kept Android-free so they can be unit-tested. */
object TimeFormat {

    /** "m:ss" for a remaining-time value, rounded up so 0.2 s still shows 0:01. */
    fun clock(millis: Long): String {
        val seconds = (millis.coerceAtLeast(0L) + 999L) / 1000L
        return String.format(Locale.US, "%d:%02d", seconds / 60, seconds % 60)
    }

    /** Grouped integer with a unit, e.g. "16,000 Hz". */
    fun frequency(hz: Int): String = String.format(Locale.US, "%,d Hz", hz)

    /** Human label for a band, e.g. "1,000–5,000 Hz". */
    fun band(startHz: Int, endHz: Int): String =
        if (startHz == endHz) frequency(startHz)
        else String.format(Locale.US, "%,d–%,d Hz", startHz, endHz)
}
