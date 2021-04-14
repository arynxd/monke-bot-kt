package me.arynxd.monke.commands.configuration

import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentLanguage
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.translation.Language

@Suppress("UNUSED")
class LanguageCommand : Command(
    name = "language",
    description = "Gets and sets the language for this server.",
    category = CommandCategory.CONFIGURATION,

    arguments = ArgumentConfiguration(
        listOf(
            ArgumentLanguage(
                name = "language",
                description = "The new language.",
                required = false,
                type = ArgumentType.REGULAR,
            )
        )
    )
) {
    override fun runSync(event: CommandEvent) {
        val cache = event.getDataCache()
        val language = cache.language

        if (!event.isArgumentPresent(0)) {
            event.replyAsync {
                type(CommandReply.Type.INFORMATION)
                title(
                    TranslationHandler.getString(
                        language = language,
                        key = "command.language.response.get_response",
                        language.commonName
                    )
                )
                footer()
                send()
            }
            return
        }

        val newLanguage = event.getArgument<Language>(0)
        if (language == newLanguage) {
            event.replyAsync {
                type(CommandReply.Type.INFORMATION)
                title(
                    TranslationHandler.getString(
                        language = language,
                        key = "command.language.response.exists_response",
                        values = arrayOf(language.commonName)
                    )
                )
                footer()
                send()
            }
            return
        }

        event.replyAsync {
            type(CommandReply.Type.INFORMATION)
            title(
                TranslationHandler.getString(
                    language = newLanguage,
                    key = "command.language.response.set_response",
                    values = arrayOf(newLanguage.commonName)
                )
            )
            footer()
            send()

            cache.language = newLanguage
        }
    }
}