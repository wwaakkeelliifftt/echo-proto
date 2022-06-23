package com.example.echo_proto.util

import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

fun String?.getTimeInMillisFromString(): Long {
    if (this == null) {
        return 0L
    }
    val format = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ROOT)
    return try {
        val result = format.parse(this)
        result.time
    } catch (e: Exception) {
        Timber.d("Exception ------->>>> ${e.message}\n\n${e.printStackTrace()}")
        System.currentTimeMillis()
    }
}

// todo: add size parser
fun Long.getDateFromLong(): String {
    val sdf = SimpleDateFormat("dd.MM.yy  \u00B7  HH:mm  ·  ?..mb", Locale.ROOT)
    val date = Calendar.getInstance().also { it.timeInMillis = this }
    return sdf.format(date.time)
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