package me.arynxd.monke.commands.misc

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent

@Suppress("UNUSED")
class PingCommand : Command(
    name = "ping",
    description = "Shows the bot's ping to discord.",
    category = CommandCategory.MISC,
    aliases = listOf("pong"),

    ) {

    override suspend fun run(event: CommandEvent) {
        event.sendEmbed(
            Embed(
                title = "Pong!",
                description = "**REST Ping**: ${event.jda.restPing.await()}ms\n\n" +
                        "**Gateway Ping**: ${event.jda.gatewayPing}ms"
            )
        )
    }
}