package me.arynxd.monke.handlers.translation

import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.ConfigHandler
import me.arynxd.monke.objects.exception.TranslationException
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.objects.translation.TranslatedLanguage
import me.arynxd.monke.util.loadResource
import me.arynxd.monke.util.readFully
import net.dv8tion.jda.api.exceptions.ParsingException
import net.dv8tion.jda.api.utils.data.DataObject
import kotlin.reflect.KClass

class TranslationHandler(
    override val monke: Monke,
    override val dependencies: List<KClass<out Handler>> = listOf(ConfigHandler::class)
) : Handler() {
    override fun onEnable() {
        val lang = Language.getLanguageOrNull(monke.handlers[ConfigHandler::class].config.preferredLanguage)
        if (lang == null) {
            internalLanguage = Language.DEFAULT
            //This is just going to be in default, english, but for consistency it gets put through translation
            LOGGER.warn(
                translateInternal { path = "internal_error.invalid_language" }
            )
            return
        }
        internalLanguage = lang
    }

    companion object {
        val languages = initLanguages()
        val keyRegex = Regex("\\.")
        private lateinit var internalLanguage: Language

        val internalLang: Language //Avoiding public mutable types
            get() = internalLanguage

        private fun initLanguages(): Map<Language, TranslatedLanguage> {
            val supportedLanguages = loadResource("assets/translation/supported_languages.txt")
                .readFully()
                .split("/")

            val result = mutableMapOf<Language, TranslatedLanguage>()

            for (language in supportedLanguages) {
                val json = try {
                    DataObject.fromJson(loadResource("assets/translation/$language.json").readFully())
                }
                catch (exception: ParsingException) {
                    throw TranslationException("Language $language is corrupt.")
                }
                result[Language.getLanguageOrThrow(language)] = TranslatedLanguage(json)
            }

            return result.toMap()
        }
    }
}

fun doTranslateInternal(key: String, vararg values: Any?): String {
    return doTranslate(TranslationHandler.internalLang, key, *values)
}

fun doTranslate(language: Language, key: String, vararg values: Any?): String {
    val json = TranslationHandler.languages[language]?.data ?: throw TranslationException(
        doTranslateInternal("internal_error.language_not_found", language.code)
    )

    if (!key.contains(".")) {
        return json.getString(key)
    }

    val path = key.split(TranslationHandler.keyRegex)
    var data = json

    for (i in 0 until path.indices.last) {
        data = if (data.hasKey(path[i]))
            data.getObject(path[i])
        else
            throw TranslationException(
                doTranslateInternal(
                    key = "internal_error.language_key_not_found",
                    values = arrayOf(
                        path.joinToString(separator = "."),
                        language.code
                    )
                )
            )
    }

    var result =
        if (data.hasKey(path.last()))
            data.getString(path.last())
        else
            throw TranslationException(
                doTranslateInternal(
                    key = "internal_error.language_key_not_found",
                    values = arrayOf(
                        path.joinToString(separator = "."),
                        language.code
                    )
                )
            )
    
    if (values.isNotEmpty()) {
        for (i in values.indices) {
            result = result.replace("%$i", values[i].toString(), true)
        }
    }

    return result
}


