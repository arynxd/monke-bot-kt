package me.arynxd.monke.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import java.net.URL
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

private val jsonFormat = Json {
    prettyPrint = true
}

fun String.isValidUrl(): Boolean = try {
    URL(this).toURI()
    true
} catch (ex: Exception) {
    false
}

fun plurifyInt(input: Int): String = if (input != 1) "s" else ""

fun plurifyLong(input: Long): String = if (input != 1L) "s" else ""

fun parseDateTime(time: TemporalAccessor?): String? = time?.let {
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(time)
}

fun parseUptime(duration: Duration): String {
    val days = duration.toDays()
    val hours = duration.toHoursPart()
    val minutes = duration.toMinutesPart()
    val seconds = duration.toSecondsPart()

    return "$days day${plurifyLong(days)}, " +
            "$hours hour${plurifyInt(hours)}, " +
            "$minutes minute${plurifyInt(minutes)}, " +
            "$seconds second${plurifyInt(seconds)}"
}

fun prettyPrintJson(json: String): String =
    jsonFormat.encodeToString(jsonFormat.serializersModule.serializer(), jsonFormat.parseToJsonElement(json))

fun splitString(input: String): List<String> = input.chunked(Message.MAX_CONTENT_LENGTH)

fun splitStringCodeblock(input: String): List<String> = input.chunked(Message.MAX_CONTENT_LENGTH - 20)

fun splitStringEllipsis(input: String): List<String> = input.chunked(Message.MAX_CONTENT_LENGTH).also {
    val last = it.last()
    if (last.length > 3) {
        last.subSequence(0, last.length - 3)
    }
}

fun cutString(input: String, length: Int): String {
    return input.substring(0, input.length.coerceAtMost(length))
}

fun markdownSanitize(input: String): String =
    MarkdownSanitizer.sanitize(input, MarkdownSanitizer.SanitizationStrategy.REMOVE)