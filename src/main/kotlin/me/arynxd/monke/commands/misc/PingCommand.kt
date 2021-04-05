package me.arynxd.monke.commands.misc

import dev.minn.jda.ktx.await
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandReply

@Suppress("UNUSED")
class PingCommand : Command(
    name = "ping",
    description = "Shows the bot's ping to discord.",
    category = CommandCategory.MISC,
    aliases = listOf("pong"),

    ) {

    override suspend fun run(event: CommandEvent) {
        event.reply {
            type(CommandReply.Type.SUCCESS)
            title("Pong!")
            val description = "**REST Ping**: ${event.jda.restPing.await()}ms\n\n" +
                    "**Gateway Ping**: ${event.jda.gatewayPing}ms"
            description(description)
            footer()
            var time = System.currentTimeMillis()
            val message = await()
            time = System.currentTimeMillis() - time
            description(description + "\n\n **Message**: ${time}ms")
            message.editMessage(build()).queue()
        }
    }
}