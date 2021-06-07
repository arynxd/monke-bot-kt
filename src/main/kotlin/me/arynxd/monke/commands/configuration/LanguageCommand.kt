package me.arynxd.monke.commands.configuration

import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.argument.types.ArgumentLanguage
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandMetaData
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.translation.Language

@Suppress("UNUSED")
class LanguageCommand : Command(
    CommandMetaData(
        name = "language",
        description = "Gets and sets the language for this server.",
        category = CommandCategory.CONFIGURATION,

        arguments = ArgumentConfiguration(
            ArgumentLanguage(
                name = "language",
                description = "The new language.",
                required = false,
                type = Type.REGULAR,
            )
        )
    )
) {
    override fun runSync(event: CommandEvent) {
        val cache = event.dataCache
        val guildLanguage = event.language
        if (!event.isArgumentPresent(0)) {
            event.replyAsync {
                    type(CommandReply.Type.INFORMATION)
                    title(
                        translate {
                            lang = guildLanguage
                            path = "command.language.response.get_response"
                            values = arrayOf(guildLanguage.commonName)
                        }
                    )
                    footer()
                    event.thread.post(this)
                }
            return
        }

        val newLanguage = event.argument<Language>(0)
        if (guildLanguage == newLanguage) {
            event.replyAsync {
                type(CommandReply.Type.INFORMATION)
                title(
                    translate {
                        lang = guildLanguage
                        path = "command.language.response.exists_response"
                        values = arrayOf(guildLanguage.commonName)
                    }
                )
                footer()
                event.thread.post(this)
            }
            return
        }

        event.replyAsync {
            type(CommandReply.Type.INFORMATION)
            title(
                translate {
                    lang = newLanguage
                    path = "command.language.response.set_response"
                    values = arrayOf(newLanguage.commonName)
                }
            )
            footer()
            cache.language = newLanguage
            event.thread.post(this)
        }
    }
}