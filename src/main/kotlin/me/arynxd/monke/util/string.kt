package me.arynxd.monke.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.translation.translateInternal
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.*

private val jsonFormat = Json {
    prettyPrint = true
}

fun String.isValidUrl(): Boolean {
    return try {
        URL(this).toURI()
        true
    }
    catch (ex: MalformedURLException) {
        false
    }
    catch (ex: URISyntaxException) {
        false
    }
}

fun String?.equalsIgnoreCase(other: String?) = this?.equals(other, true) ?: false

fun Int.plurify() = if (this != 1) "s" else ""
fun Long.plurify() = if (this != 1L) "s" else ""

suspend fun String.takeOrHaste(length: Int, monke: Monke): String {
    if (this.isBlank()) {
        return ""
    }

    if (this.length <= length) {
        return this
    }

    return postBin(this, monke.handlers.okHttpClient) ?: translateInternal {
        path = "internal_error.web_service_error"
        values = arrayOf(HASTEBIN_SERVER)
    }
}

fun parseDateTime(time: TemporalAccessor?): String? = time?.let {
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(time)
}

fun parseUptime(duration: Duration): String {
    val days = duration.toDays()
    val hours = duration.toHoursPart()
    val minutes = duration.toMinutesPart()
    val seconds = duration.toSecondsPart()

    return "$days day${days.plurify()}, " +
            "$hours hour${hours.plurify()}, " +
            "$minutes minute${minutes.plurify()}, " +
            "$seconds second${seconds.plurify()}"
}

fun String.prettyPrintJson(): String {
    val serializer = jsonFormat.serializersModule.serializer<JsonElement>()
    val jsonElement = jsonFormat.parseToJsonElement(this)
    return jsonFormat.encodeToString(serializer, jsonElement)
}

fun splitStringCodeblock(input: String): List<String> = input.chunked(Message.MAX_CONTENT_LENGTH - 20)

fun String.markdownSanitize() = MarkdownSanitizer.sanitize(this, MarkdownSanitizer.SanitizationStrategy.REMOVE)
