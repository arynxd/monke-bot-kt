package me.arynxd.monke.handlers

import me.arynxd.monke.Monke
import me.arynxd.monke.objects.exception.TranslationException
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.objects.translation.TranslatedLanguage
import me.arynxd.monke.util.convertToString
import me.arynxd.monke.util.loadResource
import net.dv8tion.jda.api.exceptions.ParsingException
import net.dv8tion.jda.api.utils.data.DataObject
import kotlin.reflect.KClass

val KEY_REGEX: Regex = Regex("\\.")

class TranslationHandler @JvmOverloads constructor(
    override val monke: Monke,
    override val dependencies: List<KClass<out Handler>> = listOf(ConfigHandler::class)
) : Handler() {
    override fun onEnable() {
        val lang = Language.getLanguageByName(monke.handlers.get(ConfigHandler::class).config.preferredLanguage)
        if (lang == null) {
            LOGGER.warn("Language specified in the config file was invalid, falling back to defaults.")
            internalLanguage = Language.DEFAULT
            return
        }
        internalLanguage = lang
    }

    companion object {
        private val languages = initLanguages()
        lateinit var internalLanguage: Language

        fun isLanguagePresent(language: Language): Boolean {
            return languages.containsKey(language)
        }

        fun getInternalString(key: String, vararg values: Any): String {
            return getString(internalLanguage, key, *values)
        }

        fun getString(language: Language, key: String, vararg values: Any): String {
            val json = languages[language]?.data ?: throw TranslationException(
                getInternalString("internal_error.language_not_found", language.code)
            )

            if (!key.contains(".")) {
                return json.getString(key)
            }

            val path = key.split(KEY_REGEX)
            var data = json

            for (i in 0 until path.indices.last) {
                data = if (data.hasKey(path[i]))
                    data.getObject(path[i])
                else
                    throw TranslationException(
                        getInternalString(
                            "internal_error.language_key_not_found",
                            path.joinToString(separator = "."),
                            language.code
                        )
                    )
            }

            var result =
                if (data.hasKey(path.last()))
                    data.getString(path.last())
                else
                    throw TranslationException(
                        getInternalString(
                            "internal_error.language_key_not_found",
                            path.joinToString(separator = "."),
                            language.code
                        )
                    )


            if (values.isNotEmpty()) {
                for (i in values.indices) {
                    result = result.replace("%$i", values[i].toString(), true)
                }
            }

            return result
        }

        private fun initLanguages(): Map<Language, TranslatedLanguage> {
            val supportedLanguages = convertToString(
                loadResource("assets/translation/supported_languages.txt")
            ).split("/")

            val result = mutableMapOf<Language, TranslatedLanguage>()

            for (language in supportedLanguages) {
                val json = try {
                        DataObject.fromJson(convertToString(loadResource("assets/translation/$language.json")))
                    }
                    catch (exception: ParsingException) {
                        throw TranslationException("Language $language is corrupt.")
                    }
                result[Language.getLanguageByCode(language)] = TranslatedLanguage(json)
            }

            return result.toMap()
        }
    }
}