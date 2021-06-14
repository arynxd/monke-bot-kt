package me.arynxd.monke.handlers.translation

import me.arynxd.monke.objects.translation.Language
import net.dv8tion.jda.internal.utils.Checks

data class MultiPartTranslationBuilder(
    val lang: Language,
) {
    private val builders = mutableListOf<TranslationBuilder>()

    fun part(path: String, vararg values: Any?) {
        builders.add(
            translationStep {
                this.path = path
                //I cant remove this unchecked cast warning >:(
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
                //I cant remove this unchecked cast warning >:(
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