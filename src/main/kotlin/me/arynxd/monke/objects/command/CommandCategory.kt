package me.arynxd.monke.objects.command

import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.translation.Language

enum class CommandCategory(private val key: String) {
    DEVELOPER("developer"),
    FUN("fun"),
    MISC("misc"),
    MODERATION("moderation"),
    CONFIGURATION("configuration"),
    MUSIC("music");

    fun getName(language: Language) = translate {
        lang = language
        path = "command.category.$key"
    }
}