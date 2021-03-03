package me.arynxd.monke.commands.misc

import dev.minn.jda.ktx.Embed
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.util.cutString
import me.arynxd.monke.util.getWikipediaPage
import me.arynxd.monke.util.parseDateTime
import me.arynxd.monke.util.sendError
import net.dv8tion.jda.api.entities.MessageEmbed

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
        val tooVague = TranslationHandler.getString(language, "command.wiki.response.not_found")
        if (page == null) {
            sendError(event.message, tooVague)
            return
        }

        val lastEdited = TranslationHandler.getString(language, "command.wiki.keyword.last_edited", parseDateTime(page.getTimestamp())?: "null")
        event.sendEmbed(Embed(
            image = if (event.channel.isNSFW) page.getThumbnail() else null,
            title = page.getTitle(),
            description = cutString(page.getExtract()?: "null", MessageEmbed.TEXT_MAX_LENGTH),
            footerText = lastEdited)
        )
    }
}