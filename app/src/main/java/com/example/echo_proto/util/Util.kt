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
    val format = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ROOT)
    return try {
        val result = format.parse(this)
        result.time
    } catch (e: Exception) {
        Timber.d(e, "Exception ------->>>> ${e.message}\n\n${e.printStackTrace()}")
        System.currentTimeMillis()
    }
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