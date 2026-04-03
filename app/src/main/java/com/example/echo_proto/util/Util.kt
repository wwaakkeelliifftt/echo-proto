package com.example.echo_proto.util

import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes

fun String?.getTimeInMillisFromString(): Long {
    if (this == null) {
        return 0L
    }
    
    // 🎯 FIXED: Use English Locale for RSS dates (they're always in English)
    val rssLocale = Locale.ENGLISH
    
    // Try multiple date formats for different RSS feeds
    val formats = listOf(
        "EEE, d MMM yyyy HH:mm:ss Z",  // Fri, 31 May 2019 15:19:48 +0000
        "EEE, d MMM yyyy HH:mm:ss z",  // Wed, 03 Apr 2024 12:00:00 GMT
        "d MMM yyyy HH:mm:ss Z",       // 31 May 2019 15:19:48 +0000
        "yyyy-MM-dd HH:mm:ss",         // 2019-05-31 15:19:48
        "yyyy-MM-dd'T'HH:mm:ss'Z'",     // 2019-05-31T15:19:48Z
        "EEE, d MMM yyyy HH:mm:ss 'UTC'" // Sat, 28 Mar 2026 18:03:36 UTC
    )
    
    for (formatString in formats) {
        try {
            val format = SimpleDateFormat(formatString, rssLocale)
            val result = format.parse(this)
            Timber.d("✅ Date parsing SUCCESS: '$this' with format '$formatString' -> ${result.time}")
            return result.time
        } catch (e: Exception) {
            // Try next format
        }
    }
    
    Timber.e("🚨 Date parsing FAILED for all formats: '$this'")
    Timber.e("🚨 Using current time as fallback - this will show wrong date!")
    return System.currentTimeMillis()
}

// todo: add size parser
fun Long.getDateFromLong(): String {
    val sdf = SimpleDateFormat("dd.MM.yy", Locale.ROOT) //  \u00B7  HH:mm  ·  ?..mb", Locale.ROOT)
    val date = Calendar.getInstance().also { it.timeInMillis = this }
    return sdf.format(date.time)
}

fun Long.getCurrentTimeFromLong(): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val data = Calendar.getInstance().also { it.timeInMillis = this }
    return sdf.format(data.time)
}

fun Int.getSizeFromTimeDuration(): String {
    val minutes = this / 60.0
    val multiplyer = minutes * 1.2
    return "${String.format(Locale.ROOT, "%.0f", multiplyer)} Mb"
}

fun String.checkLessThenHour(): String {
    if (this.startsWith("00:") && this.count { it == ':' } == 2) {
        return this.substringAfter(':')
    }
    return this
}

fun Int.getTimeFromSeconds(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60

    fun output(digit: Int): String = when {
        digit == 0 -> "00"
        (digit / 10 == 0) -> "0$digit"
        else -> digit.toString()
    }
    return "${output(hours)}:${output(minutes)}:${output(seconds)}"
}

fun Float.normalizePlaybackSpeed(): Float {
    val clamped = this.coerceIn(Constants.PLAYBACK_SPEED_MIN, Constants.PLAYBACK_SPEED_MAX)
    val steps = (clamped / Constants.PLAYBACK_SPEED_STEP).roundToInt()
    return (steps * Constants.PLAYBACK_SPEED_STEP).let {
        // avoid floating errors
        String.format(Locale.US, "%.2f", it).toFloat()
    }
}

fun Float?.isCloseTo(other: Float, epsilon: Float = 0.01f): Boolean {
    if (this == null) return false
    return abs(this - other) < epsilon
}