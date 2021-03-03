package me.arynxd.monke.commands.developer

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandFlag
import me.arynxd.monke.util.ERROR_EMBED_COLOUR
import me.arynxd.monke.util.SUCCESS_EMBED_COLOUR
import me.arynxd.monke.util.awaitConfirmation
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
        val confirm = TranslationHandler.getString(language, "command.shutdown.response.confirmation")
        val aborted = TranslationHandler.getString(language, "command.shutdown.response.aborted")
        val success = TranslationHandler.getString(language, "command.shutdown.response.success")

        val message = event.channel.sendMessage(Embed(
            title = confirm,
            color = ERROR_EMBED_COLOUR.rgb
        )).await()

        val confirmation = awaitConfirmation(
            message = message,
            user = event.user,
            monke = event.monke
        )

        if (confirmation == null) {
            message.delete().queue()
            return
        }

        if (!confirmation) {
            message.editMessage(Embed(
                title = aborted,
                color = SUCCESS_EMBED_COLOUR.rgb
            )).queue()
            message.clearReactions().queue()
            return
        }

        message.editMessage(Embed(
            title = success,
            color = SUCCESS_EMBED_COLOUR.rgb
        )).await()
        message.clearReactions().await()

        event.monke.handlers.disableHandlers()
        event.jda.shutdownNow()
        exitProcess(0)
    }
}