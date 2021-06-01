package me.arynxd.monke.commands.misc

import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandMetaData
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.objects.command.CommandEvent

@Suppress("UNUSED")
class UptimeCommand : Command(
    CommandMetaData(
        name = "uptime",
        description = "Shows the bot's uptime.",
        category = CommandCategory.MISC
    )
) {

    override fun runSync(event: CommandEvent) {
        val language = event.language()
        val uptime = translate(language, "command.uptime.keyword.uptime")
        event.replyAsync {
            type(CommandReply.Type.SUCCESS)
            title("$uptime: ${event.monke.getUptimeString()}")
            footer()
            event.thread.post(this)
        }
    }
}