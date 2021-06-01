package me.arynxd.monke.commands.misc

import dev.minn.jda.ktx.await
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.threads.CommandReply

@Suppress("UNUSED")
class PingCommand : Command(
    CommandMetaData(
        name = "ping",
        description = "Shows the bot's ping to discord.",
        category = CommandCategory.MISC,
        flags = listOf(CommandFlag.SUSPENDING),
        aliases = listOf("pong")
    )
) {

    override suspend fun runSuspend(event: CommandEvent) {
        event.reply {
            type(CommandReply.Type.SUCCESS)
            title("Pong!")

            val description =
                "**REST Ping**: ${event.jda.restPing.await()}ms\n\n" +
                        "**Gateway Ping**: ${event.jda.gatewayPing}ms"

            description(description)
            footer()
            var time = System.currentTimeMillis()
            val message = event.thread.awaitPost(this)
            time = System.currentTimeMillis() - time
            description(description + "\n\n **Message**: ${time}ms")
            message.editMessage(build()).queue()
        }
    }
}