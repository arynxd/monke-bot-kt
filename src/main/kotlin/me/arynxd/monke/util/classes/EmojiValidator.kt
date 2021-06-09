package me.arynxd.monke.util.classes

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.util.loadResource
import me.arynxd.monke.util.readFully

object EmojiValidator {
    private val setOfEmoji = mutableSetOf<String>()

    fun init() {
        val emojiJson = loadResource("assets/emoji.json").readFully()

        if (emojiJson.isBlank()) {
            LOGGER.warn("emoji.json not found, validation cannot be performed on unicode emojis.")
            return
        }

        val jsonParse = Json { isLenient = true }

        try {
            val json = jsonParse.parseToJsonElement(emojiJson)

            for (obj in json.jsonObject) {
                val jsonObj = obj.value.jsonObject
                val emojiObj = jsonObj["emoji"] ?: throw IllegalStateException("json did not have 'emoji' key")
                val emoji = emojiObj.jsonPrimitive.content
                setOfEmoji.add(emoji)

                val diversity = jsonObj["diversity"] ?: continue

                for (diverse in diversity.jsonObject) {
                    val div = diverse.value.jsonPrimitive.content
                    setOfEmoji.add(div)
                }
            }
        }
        catch (exception: Exception) {
            LOGGER.error("emoji.json was corrupt", exception)
            return
        }
        LOGGER.info("emoji.json loaded successfully")
    }

    fun unicodeExists(unicode: String) = setOfEmoji.contains(unicode)
}