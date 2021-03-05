package me.arynxd.monke.commands.developer

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandFlag
import me.arynxd.monke.util.SUCCESS_EMBED_COLOUR
import kotlin.system.exitProcess

@Suppress("UNUSED")
class ShutdownCommand : Command(
    name = "shutdown",
    description = "Shuts the bot down gracefully.",
    category = CommandCategory.DEVELOPER,
    flags = listOf(CommandFlag.DEVELOPER_ONLY),
) {
    override suspend fun run(event: CommandEvent) {
        val language = event.getLanguage()
        val success = TranslationHandler.getString(language, "command.shutdown.response.success")

        event.channel.sendMessage(
            Embed(
                title = success,
                color = SUCCESS_EMBED_COLOUR.rgb
            )
        ).await()

        event.monke.handlers.disableHandlers()
        event.jda.shutdownNow()
        exitProcess(0)
    }
}