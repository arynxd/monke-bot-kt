package me.arynxd.monke.handlers

import me.arynxd.monke.Monke
import me.arynxd.monke.objects.exception.TranslationException
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.objects.translation.TranslatedLanguage
import me.arynxd.monke.util.loadResource
import me.arynxd.monke.util.readFully
import net.dv8tion.jda.api.exceptions.ParsingException
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.utils.Checks
import kotlin.reflect.KClass

class TranslationHandler(
    override val monke: Monke,
    override val dependencies: List<KClass<out Handler>> = listOf(ConfigHandler::class)
) : Handler() {
    override fun onEnable() {
        val lang = Language.getLanguageOrNull(monke.handlers[ConfigHandler::class].config.preferredLanguage)
        if (lang == null) {
            LOGGER.warn("Language specified in the config file was invalid, falling back to defaults.")
            internalLanguage = Language.DEFAULT
            return
        }
        internalLanguage = lang
    }

    companion object {
        val languages = initLanguages()
        val keyRegex: Regex = Regex("\\.")
        lateinit var internalLanguage: Language

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

private fun doTranslateInternal(key: String, vararg values: Any?): String {
    return doTranslate(TranslationHandler.internalLanguage, key, *values)
}

private fun doTranslate(language: Language, key: String, vararg values: Any?): String {
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

fun translate(fn: TranslationBuilder.() -> Unit): String {
    val builder = TranslationBuilder(null, null, emptyArray(), false)
    fn(builder)
    return builder.build()
}

fun translateInternal(fn: TranslationBuilder.() -> Unit): String {
    val builder = TranslationBuilder(null, null, emptyArray(), true)
    fn(builder)
    return builder.build()
}

private fun translationStep(fn: TranslationBuilder.() -> Unit): TranslationBuilder {
    val builder = TranslationBuilder(null, null, emptyArray(), false)
    fn(builder)
    return builder
}

private fun translationStepInternal(fn: TranslationBuilder.() -> Unit): TranslationBuilder {
    val builder = TranslationBuilder(null, null, emptyArray(), true)
    fn(builder)
    return builder
}

fun translateAll(lang: Language, fn: MultiPartTranslationBuilder.() -> Unit): List<String> {
    val builder = MultiPartTranslationBuilder(lang)
    fn(builder)
    return builder.buildAll()
}

data class MultiPartTranslationBuilder(
    val lang: Language,
) {
    private val builders = mutableListOf<TranslationBuilder>()

    fun part(path: String, vararg values: Any?) {
        builders.add(
            translationStep {
                this.path = path
                this.values = values as Array<Any?>
                if (this.lang == null) {
                    this.lang = this@MultiPartTranslationBuilder.lang
                }
            }
        )
    }

    fun partInternal(path: String, vararg values: Any?) {
        builders.add(
            translationStepInternal {
                this.path = path
                this.values = values as Array<Any?>
                if (this.lang == null) {
                    this.lang = this@MultiPartTranslationBuilder.lang
                }
            }
        )
    }

    fun buildAll(): List<String> {
        return builders.map { it.build() }
    }
}

class TranslationBuilder(
    var lang: Language?,
    var path: String?,
    var values: Array<Any?>,
    val isInternal: Boolean
) {
    private fun validate() {
        Checks.notNull(lang, "Language")
        Checks.notNull(path, "Key")
    }

    fun build(): String {
        validate()
        return if (isInternal) doTranslateInternal(path!!, *values) else doTranslate(lang!!, path!!, *values)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TranslationBuilder

        if (lang != other.lang) return false
        if (path != other.path) return false
        if (!values.contentEquals(other.values)) return false
        if (isInternal != other.isInternal) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lang?.hashCode() ?: 0
        result = 31 * result + (path?.hashCode() ?: 0)
        result = 31 * result + values.contentHashCode()
        result = 31 * result + isInternal.hashCode()
        return result
    }
}