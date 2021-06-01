package me.arynxd.monke.commands.developer

import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.threads.CommandReply
import kotlin.system.exitProcess

@Suppress("UNUSED")
class ShutdownCommand : Command(
    CommandMetaData(
        name = "shutdown",
        description = "Shuts the bot down gracefully.",
        category = CommandCategory.DEVELOPER,
        flags = listOf(CommandFlag.DEVELOPER_ONLY, CommandFlag.SUSPENDING)
    )
) {
    override suspend fun runSuspend(event: CommandEvent) {
        event.reply {
            type(CommandReply.Type.SUCCESS)
            title(
                translate(
                    language = event.language(),
                    key = "command.shutdown.response.success"
                )
            )
            event.thread.awaitPost(this)
        }

        event.monke.handlers.disableHandlers()
        event.monke.plugins.disablePlugins()
        event.jda.shutdownNow()
        exitProcess(0)
    }
}