package me.arynxd.monke.commands.misc

import dev.minn.jda.ktx.await
import me.arynxd.monke.handlers.translation.translateAll
import me.arynxd.monke.objects.command.*
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
            val (ping, message, gateway, rest) = translateAll(event.language) {
                part("command.ping.keyword.ping")
                part("command.ping.keyword.message")
                part("command.ping.keyword.gateway")
                part("command.ping.keyword.rest")
            }
            val description =
                "**$rest $ping**: ${event.jda.restPing.await()}ms\n\n" +
                "**$gateway $ping**: ${event.jda.gatewayPing}ms"

            description(description)
            footer()

            var time = System.currentTimeMillis()
            val msg = event.thread.awaitPost(this)
            time = System.currentTimeMillis() - time

            description(description + "\n\n **$message**: ${time}ms")
            msg.editMessageEmbeds(build()).queue()
        }
    }
}