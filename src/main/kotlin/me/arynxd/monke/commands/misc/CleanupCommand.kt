package me.arynxd.monke.commands.misc

import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.argument.types.ArgumentInt
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandMetaData
import me.arynxd.monke.objects.command.threads.CommandReply

@Suppress("UNUSED")
class CleanupCommand : Command(
    CommandMetaData(
        name = "cleanup",
        description = "Cleans up the bot's message",
        category = CommandCategory.MISC,

        arguments = ArgumentConfiguration(
            ArgumentInt(
                name = "amount",
                description = "The amount of messages to check. Must be less than 25",
                required = false,
                type = Type.REGULAR,
                condition = { it < 30 }
            )
        )
    )
) {
    override fun runSync(event: CommandEvent) {
        val amount = event.argument(0, 20)
        event.channel.iterableHistory
            .takeAsync(amount)
            .thenApply { list ->
                list
                    .filter { it.author.idLong == event.selfMember.idLong }
                    .map { it.id }
            }
            .thenAccept {
                if (it.isEmpty()) {
                    event.replyAsync {
                        type(CommandReply.Type.EXCEPTION)
                        title("No messages found")
                        footer()
                        event.thread.post(this)
                    }
                    return@thenAccept
                }

                event.channel.purgeMessagesById(it)
                event.replyAsync {
                    type(CommandReply.Type.SUCCESS)
                    title("Cleaned up ${it.size} messages")
                    event.thread.post(this)
                }
            }
    }
}