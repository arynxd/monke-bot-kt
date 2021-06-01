package me.arynxd.monke.commands.moderation

import dev.minn.jda.ktx.asFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import me.arynxd.monke.handlers.RateLimitHandler
import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.argument.types.ArgumentInt
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandMetaData
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.ratelimit.RateLimitedAction
import me.arynxd.monke.util.plurify
import net.dv8tion.jda.api.Permission

@Suppress("UNUSED")
class ClearCommand : Command(
    CommandMetaData(
        name = "clear",
        description = "Clears messages from this channel.",
        category = CommandCategory.MODERATION,
        aliases = listOf("purge"),
        cooldown = 10_000L,
        arguments = ArgumentConfiguration(
            ArgumentInt(
                name = "amount",
                description = "The amount to clear. 1 - 50",
                required = true,
                type = Type.REGULAR,
                condition = { it in 1..50 },
            )
        ),
        memberPermissions = listOf(Permission.MESSAGE_MANAGE),
        botPermissions = listOf(Permission.MESSAGE_MANAGE)
    )
) {

    override fun runSync(event: CommandEvent) {
        val limiter = event.monke.handlers[RateLimitHandler::class].getRateLimiter(event.guildIdLong)
        val language = event.language()
        val thread = event.thread

        if (!limiter.canTake(RateLimitedAction.BULK_DELETE)) {
            val resp = event.replyAsync {
                type(CommandReply.Type.EXCEPTION)
                title(
                    translate(
                        language = language,
                        key = "command_error.rate_limited"
                    )
                )
                footer()
            }
            event.thread.post(resp)
            return
        }

        val amount = event.argument<Int>(0)
        val amountToTake = if (thread.hasPosts) amount + 2 else amount + 1
        event.channel.iterableHistory
            .takeAsync(amountToTake) //Account for 1 based indexing from the user + ignoring the users message
            .thenApply { list ->
                list.filter { //Dont remove the original message + our reply if one exists as we need to reply to it
                    it.idLong != event.message.idLong && !event.thread.responseIds.contains(it.idLong)
                }
            }
            .thenAccept {
                event.channel.purgeMessages(it)
                event.replyAsync {
                    type(CommandReply.Type.SUCCESS)
                    title(
                        translate(
                            language = language,
                            key = "command.clear.response.cleared",
                            values = arrayOf(
                                amount,
                                amount.plurify()
                            )
                        )
                    )
                    footer()
                    limiter.take(RateLimitedAction.BULK_DELETE)
                    event.thread.post(this)
                }
            }

    }
}