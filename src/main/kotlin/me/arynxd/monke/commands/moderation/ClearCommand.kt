package me.arynxd.monke.commands.moderation

import me.arynxd.monke.handlers.RateLimitHandler
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentInt
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.ratelimit.RateLimitedAction
import me.arynxd.monke.util.plurifyInt
import net.dv8tion.jda.api.Permission

@Suppress("UNUSED")
class ClearCommand : Command(
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
                type = ArgumentType.REGULAR,
                condition = { it in 1..50 },
            )
        )
    ),
    memberPermissions = listOf(Permission.MESSAGE_MANAGE),

    botPermissions = listOf(Permission.MESSAGE_MANAGE),

    ) {

    override suspend fun run(event: CommandEvent) {
        val limiter = event.monke.handlers.get(RateLimitHandler::class).getRateLimiter(event.guildIdLong)
        val language = event.getLanguage()

        if (!limiter.canTake(RateLimitedAction.BULK_DELETE)) {
            event.reply {
                exception()
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
            .takeAsync(event.getArgument<Int>(0) + 2)
            .thenApply {
                it.filter { m -> m.idLong != event.message.idLong }
            }
            .thenAccept {
                event.channel.purgeMessages(it)
                event.replyAsync {
                    success()
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
                }
                limiter.take(RateLimitedAction.BULK_DELETE)
            }
    }
}