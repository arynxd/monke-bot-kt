package me.arynxd.monke.commands.misc

import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.util.getWikipediaPage
import me.arynxd.monke.util.parseDateTime

@Suppress("UNUSED")
class WikipediaCommand : Command(
    name = "wiki",
    description = "Queries Wikipedia with the given text.",
    category = CommandCategory.MISC,
    aliases = listOf("wikipedia"),
    cooldown = 3000L,

    arguments = ArgumentConfiguration(
        listOf(
            ArgumentString(
                name = "subject",
                description = "The subject to query Wikipedia with.",
                required = true,
                type = ArgumentType.VARARG
            )
        )
    ),

    ) {
    override suspend fun run(event: CommandEvent) {
        val subject = event.getVararg<String>(0).joinToString(separator = "_").let {
            if (it == "cbt") "Cock_and_ball_torture"
            else it
        }

        val language = event.getLanguage()

        val page = getWikipediaPage(event, subject)
        if (page == null) {
            event.reply {
                exception()
                title(
                    TranslationHandler.getString(
                        language = language,
                        key = "command.wiki.response.not_found"
                    )
                )
                send()
            }
            return
        }

        val lastEdited = TranslationHandler.getString(
            language = language,
            key = "command.wiki.keyword.last_edited",
            values = arrayOf(
                parseDateTime(page.getTimestamp()) ?: "null"
            )
        )

        event.reply {
            success()
            title(page.getTitle())
            description(page.getExtract() ?: "null")
            footer(lastEdited)
            send()
        }
    }
}