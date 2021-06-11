package me.arynxd.monke.commands.moderation

import dev.minn.jda.ktx.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import me.arynxd.monke.handlers.RateLimitHandler
import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentResult
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.argument.types.ArgumentInt
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.objects.ratelimit.RateLimitedAction
import me.arynxd.monke.util.plurify
import net.dv8tion.jda.api.Permission

@Suppress("UNUSED")
class ClearCommand : Command(
    CommandMetaData(
        name = "clear",
        description = "Clears messages from this channel.",
        category = CommandCategory.MODERATION,
        flags = listOf(CommandFlag.SUSPENDING),
        cooldown = 10_000L,
        arguments = ArgumentConfiguration(
            ArgumentInt(
                name = "amount",
                description = "The amount to clear.",
                required = true,
                type = Type.REGULAR,
                condition = {
                    if (it !in 1..50)
                        ArgumentResult(null, "internal_error.nop") //TODO add translation here
                    else
                        ArgumentResult(it, null)

                },
            )
        ),
        memberPermissions = listOf(Permission.MESSAGE_MANAGE),
        botPermissions = listOf(Permission.MESSAGE_MANAGE)
    )
) {

    override suspend fun runSuspend(event: CommandEvent) {
        val limiter = event.monke.handlers[RateLimitHandler::class].getRateLimiter(event.guildIdLong)
        val language = event.language
        val thread = event.thread

        if (!limiter.canTake(RateLimitedAction.BULK_DELETE)) {
            val resp = event.replyAsync {
                type(CommandReply.Type.EXCEPTION)
                title(
                    translate {
                        lang = language
                        path = "command_error.rate_limited"
                    }
                )
                footer()
            }
            event.thread.post(resp)
            return
        }

        val amount = event.argument<Int>(0)
        val amountToTake = if (thread.hasPosts) amount + 2 else amount + 1

        val messages = event.channel.iterableHistory.asFlow()
            .take(amountToTake)
            .filter { it.idLong != event.message.idLong && !event.thread.contains(it.idLong) }
            .toList()

        event.channel.purgeMessages(messages)
        event.replyAsync {
            type(CommandReply.Type.SUCCESS)
            title(
                translate {
                    lang = language
                    path = "command.clear.response.cleared"
                    values = arrayOf(
                        amount,
                        amount.plurify()
                    )
                }
            )
            footer()
            limiter.take(RateLimitedAction.BULK_DELETE)
            event.thread.post(this)
        }
    }
}