package me.arynxd.monke.commands.developer

import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.command.*
import kotlin.system.exitProcess

@Suppress("UNUSED")
class ShutdownCommand : Command(
    name = "shutdown",
    description = "Shuts the bot down gracefully.",
    category = CommandCategory.DEVELOPER,
    flags = listOf(CommandFlag.DEVELOPER_ONLY, CommandFlag.ASYNC),
) {
    override suspend fun runSuspend(event: CommandEvent) {
        event.reply {
            type(CommandReply.Type.SUCCESS)
            title(
                TranslationHandler.getString(
                    language = event.getLanguage(),
                    key = "command.shutdown.response.success"
                )
            )
            footer()
            await()
        }

        event.monke.handlers.disableHandlers()
        event.monke.plugins.disablePlugins()
        event.jda.shutdownNow()
        exitProcess(0)
    }
}