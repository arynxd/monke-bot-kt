package me.arynxd.monke.commands.moderation

import me.arynxd.monke.handlers.RateLimitHandler
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.argument.types.ArgumentInt
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandMetaData
import me.arynxd.monke.objects.command.CommandReply
import me.arynxd.monke.objects.events.types.command.CommandEvent
import me.arynxd.monke.objects.ratelimit.RateLimitedAction
import me.arynxd.monke.util.plurifyInt
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
            listOf(
                ArgumentInt(
                    name = "amount",
                    description = "The amount to clear. 1 - 50",
                    required = true,
                    type = Type.REGULAR,
                    condition = { it in 1..50 },
                )
            )
        ),
        memberPermissions = listOf(Permission.MESSAGE_MANAGE),
        botPermissions = listOf(Permission.MESSAGE_MANAGE)
    )
) {

    override fun runSync(event: CommandEvent) {
        val limiter = event.monke.handlers.get(RateLimitHandler::class).getRateLimiter(event.guildIdLong)
        val language = event.getLanguage()

        if (!limiter.canTake(RateLimitedAction.BULK_DELETE)) {
            event.replyAsync {
                type(CommandReply.Type.EXCEPTION)
                title(
                    TranslationHandler.getString(
                        language = language,
                        key = "command_error.rate_limited"
                    )
                )
                footer()
                send()
            }
            return
        }

        event.channel.iterableHistory
            .takeAsync(event.getArgument<Int>(0) + 2) //Account for 1 based indexing from the user + ignoring the users message
            .thenApply { list ->
                list.filter { //Dont remove the original message as we need to reply to it
                    it.idLong != event.message.idLong
                }
            }
            .thenAccept {
                event.channel.purgeMessages(it)
                event.replyAsync {
                    type(CommandReply.Type.SUCCESS)
                    title(
                        TranslationHandler.getString(
                            language = language,
                            key = "command.clear.response.cleared",
                            values = arrayOf(
                                it.size - 1,
                                plurifyInt(it.size - 1)
                            )
                        )
                    )
                    footer()
                    send()
                    limiter.take(RateLimitedAction.BULK_DELETE)
                }
            }
    }
}