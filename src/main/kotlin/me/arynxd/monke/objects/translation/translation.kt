package me.arynxd.monke.objects.translation

import me.arynxd.monke.objects.exception.TranslationException
import net.dv8tion.jda.api.utils.data.DataObject

data class TranslatedLanguage(val data: DataObject)

enum class Language(val code: String, val aliases: List<String>, val commonName: String) {
    EN_US("en_US", listOf("english"), "English"),
    DEFAULT("en_US", listOf("english"), "English");
//    DE("de", listOf("deutsch", "german"), "Deutsch");

    companion object {
        fun getLanguageByCode(code: String): Language {
            return values().find { it.code == code }?: throw TranslationException("Language $code was not found.")
        }

        fun getLanguageByName(name: String): Language? {
            return values().find { it.code == name || it.aliases.contains(name.toLowerCase())}
        }

        fun getLanguages(): List<Language> {
            return values().filter { it != DEFAULT }
        }
    }
}

