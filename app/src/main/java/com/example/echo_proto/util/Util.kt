package com.example.echo_proto.util

import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToInt

fun String?.getTimeInMillisFromString(): Long {
    if (this == null) {
        return 0L
    }
    
    val rssLocale = Locale.ENGLISH
    
    val formats = listOf(
        "EEE, d MMM yyyy HH:mm:ss Z",
        "EEE, d MMM yyyy HH:mm:ss z",
        "d MMM yyyy HH:mm:ss Z",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "EEE, d MMM yyyy HH:mm:ss 'UTC'"
    )
    
    for (formatString in formats) {
        try {
            val format = SimpleDateFormat(formatString, rssLocale)
            val result = format.parse(this)
            return result.time
        } catch (e: Exception) {
        }
    }
    return System.currentTimeMillis()
}

fun Long.getDateFromLong(): String {
    val sdf = SimpleDateFormat("dd.MM.yy", Locale.ROOT)
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

/**
 * Конвертирует строку таймкода (01:22:33 или 22:33) в миллисекунды
 */
fun String.timestampToMillis(): Long {
    val parts = this.split(":").map { it.toLongOrNull() ?: 0L }
    return when (parts.size) {
        3 -> (parts[0] * 3600 + parts[1] * 60 + parts[2]) * 1000
        2 -> (parts[0] * 60 + parts[1]) * 1000
        else -> 0L
    }
}

fun Float.normalizePlaybackSpeed(): Float {
    val clamped = this.coerceIn(Constants.PLAYBACK_SPEED_MIN, Constants.PLAYBACK_SPEED_MAX)
    val steps = (clamped / Constants.PLAYBACK_SPEED_STEP).roundToInt()
    return (steps * Constants.PLAYBACK_SPEED_STEP).let {
        if (it < 0) Constants.PLAYBACK_SPEED_MIN
        else if (it > Constants.PLAYBACK_SPEED_MAX) Constants.PLAYBACK_SPEED_MAX
        else it
    }
}

/**
 * Выделяет таймкоды в тексте и оборачивает их в span с цветом Nocturne Gold
 */
fun String.highlightTimestamps(): String {
    val goldColor = "#E6AF2E" 
    val timestampPattern = Regex("""\b(\d{1,2}:\d{2}(?::\d{2})?)\b""")

    return timestampPattern.replace(this) { match ->
        val timestamp = match.value
        // Добавляем пробелы по бокам, чтобы фон выглядел как рамочка
        "<a href=\"seek://$timestamp\" style=\"text-decoration: none;\">&nbsp;$timestamp&nbsp;</a>"
    }
}

/**
 * Конвертирует URL в тексте в кликабельные HTML ссылки Nocturne Gold
 */
fun String.makeLinksClickable(): String {
    val goldColor = "#E6AF2E"
    val urlPattern = Regex("""\b((?:https?://|www\.)[^\s<>]+)\b""")
    
    return urlPattern.replace(this) { match ->
        val url = match.value
        val fullUrl = if (url.startsWith("www.")) "https://$url" else url
        "<a href=\"$fullUrl\" target=\"_blank\" rel=\"noopener noreferrer\" style=\"color: $goldColor; text-decoration: underline; font-weight: 600;\">$url</a>"
    }
}

fun String.enrichForWebView(): String {
    return if (this.containsHtmlTags()) {
        this.highlightTimestamps()
    } else {
        this.makeLinksClickable().highlightTimestamps()
    }
}

private fun String.containsHtmlTags(): Boolean {
    val htmlPatterns = listOf(
        Regex("""<a[^>]+>"""),
        Regex("""<img[^>]+>"""),
        Regex("""<p[^>]*>"""),
        Regex("""<strong>"""),
        Regex("""<em>"""),
        Regex("""<ul>"""),
        Regex("""<ol>"""),
        Regex("""<li>""")
    )
    return htmlPatterns.any { pattern -> pattern.containsMatchIn(this) }
}

fun Float?.isCloseTo(other: Float, epsilon: Float = 0.01f): Boolean {
    if (this == null) return false
    return abs(this - other) < epsilon
}
