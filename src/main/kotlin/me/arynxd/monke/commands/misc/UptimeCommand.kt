package me.arynxd.monke.commands.misc

import dev.minn.jda.ktx.Embed
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent

@Suppress("UNUSED")
class UptimeCommand : Command(
    name = "uptime",
    description = "Shows the bot's uptime.",
    category = CommandCategory.MISC,
) {

    override suspend fun run(event: CommandEvent) {
        val language = event.getLanguage()
        val uptime = TranslationHandler.getString(language, "command.uptime.keyword.uptime")
        event.sendEmbed(Embed(
            title = "$uptime: ${event.monke.getUptimeString()}"
        ))
    }
}