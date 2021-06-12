package me.arynxd.monke.commands.misc

import dev.minn.jda.ktx.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.types.ArgumentRange
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.command.threads.CommandReply

@Suppress("UNUSED")
class CleanupCommand : Command(
    CommandMetaData(
        name = "cleanup",
        description = "Cleans up the bot's message",
        category = CommandCategory.MISC,
        flags = listOf(CommandFlag.SUSPENDING),

        arguments = ArgumentConfiguration(
            ArgumentRange(
                name = "amount",
                description = "The amount of messages to check.",
                required = true,
                type = Argument.Type.REGULAR,
                upperBound = 30,
                lowerBound = 1,
            )
        )
    )
) {
    override suspend fun runSuspend(event: CommandEvent) {
        val amount = event.argument(0, 20L)
        val language = event.language

        val messages = event.channel.iterableHistory.asFlow()
            .take(amount.toInt())
            .filter { it.author.idLong == event.selfMember.idLong }
            .toList()

        if (messages.isEmpty()) {
            event.replyAsync {
                type(CommandReply.Type.EXCEPTION)
                title(
                    translate {
                        lang = language
                        path = "command.cleanup.response.no_messages"
                    }
                )
                footer()
                event.thread.post(this)
            }
            return
        }

        event.channel.purgeMessagesById(messages.map { it.toString() })
        event.replyAsync {
            type(CommandReply.Type.SUCCESS)
            title(
                translate {
                    lang = language
                    path = "command.cleanup.response.success"
                    values = arrayOf(messages.size)
                }
            )
            event.thread.post(this)
        }
    }
}