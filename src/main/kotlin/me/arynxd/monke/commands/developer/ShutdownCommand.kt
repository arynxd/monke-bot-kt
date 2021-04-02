package me.arynxd.monke.commands.developer

import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandFlag
import kotlin.system.exitProcess

@Suppress("UNUSED")
class ShutdownCommand : Command(
    name = "shutdown",
    description = "Shuts the bot down gracefully.",
    category = CommandCategory.DEVELOPER,
    flags = listOf(CommandFlag.DEVELOPER_ONLY),
) {
    override suspend fun run(event: CommandEvent) {
        event.reply {
            success()
            title(
                TranslationHandler.getString(
                    language = event.getLanguage(),
                    key = "command.shutdown.response.success"
                )
            )
            await()
        }

        event.monke.handlers.disableHandlers()
        event.jda.shutdownNow()
        exitProcess(0)
    }
}