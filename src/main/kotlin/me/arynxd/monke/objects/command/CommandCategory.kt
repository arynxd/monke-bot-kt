package me.arynxd.monke.objects.command

import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.translation.Language

enum class CommandCategory(private val key: String) {
    DEVELOPER("developer"),
    FUN("fun"),
    MISC("misc"),
    MODERATION("moderation"),
    CONFIGURATION("configuration");

    fun getName(language: Language): String {
        return TranslationHandler.getString(language, "command.category.$key")
    }
}