package me.arynxd.monke.commands.misc

import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandMetaData
import me.arynxd.monke.objects.command.CommandReply
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.util.getWikipediaPage
import me.arynxd.monke.util.parseDateTime

@Suppress("UNUSED")
class WikipediaCommand : Command(
    CommandMetaData(
        name = "wiki",
        description = "Queries Wikipedia with the given text.",
        category = CommandCategory.MISC,
        aliases = listOf("wikipedia"),
        cooldown = 3000L,

        arguments = ArgumentConfiguration(
            ArgumentString(
                name = "subject",
                description = "The subject to query Wikipedia with.",
                required = true,
                type = Type.VARARG
            )
        )
    )
) {
    override suspend fun runSuspend(event: CommandEvent) {
        val subject = event.vararg<String>(0).joinToString(separator = "_").let {
            if (it == "cbt") "Cock_and_ball_torture"
            else it
        }

        val language = event.language()

        val page = getWikipediaPage(event, subject)
        if (page == null) {
            event.reply {
                type(CommandReply.Type.EXCEPTION)
                title(
                    translate(
                        language = language,
                        key = "command.wiki.response.not_found"
                    )
                )
                send()
            }
            return
        }

        val lastEdited = translate(
            language = language,
            key = "command.wiki.keyword.last_edited",
            values = arrayOf(
                parseDateTime(page.getTimestamp()).toString()
            )
        )

        event.reply {
            type(CommandReply.Type.SUCCESS)
            title(page.getTitle())
            description(page.getExtract().toString())
            footer(lastEdited)
            send()
        }
    }
}