package me.arynxd.monke.commands.misc

import me.arynxd.monke.handlers.RateLimitHandler
import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.argument.types.ArgumentURL
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandMetaData
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.ratelimit.RateLimitedAction
import me.arynxd.monke.util.getIcon
import net.dv8tion.jda.api.Permission
import java.net.URL

val EMOJI_REGEX: Regex = Regex("([A-Z]|[a-z]|_){2,32}")

@Suppress("UNUSED")
class StealCommand : Command(
    CommandMetaData(
        name = "steal",
        description = "Steals an emote and adds it here.",
        category = CommandCategory.MISC,

        botPermissions = listOf(Permission.MANAGE_EMOTES),
        memberPermissions = listOf(Permission.MANAGE_EMOTES),

        cooldown = 180_000L, // 2 Minutes

        arguments = ArgumentConfiguration(
            ArgumentString(
                name = "name",
                description = "The new emoji name, ( must be A-Z or _ and 2 - 32 characters long ).",
                required = true,
                type = Type.REGULAR,
                condition = { it.matches(EMOJI_REGEX) }
            ),
            ArgumentURL(
                name = "emoji",
                description = "The emoji. Must be a valid image URL.",
                required = true,
                type = Type.REGULAR,
            )
        )
    )
) {

    override fun runSync(event: CommandEvent) {
        val name = event.argument<String>(0)
        val url = event.argument<URL>(1)
        val language = event.language()
        val icon = getIcon(url)
        val limiter = event.monke.handlers.get(RateLimitHandler::class).getRateLimiter(event.guildIdLong)

        if (!limiter.canTake(RateLimitedAction.EMOJI_CREATE)) {
            event.replyAsync {
                type(CommandReply.Type.EXCEPTION)
                title(
                    translate(
                        language = language,
                        key = "command_error.rate_limited"
                    )
                )
                event.thread.post(this)
            }
            return
        }

        if (icon == null) {
            event.replyAsync {
                type(CommandReply.Type.EXCEPTION)
                title(
                    translate(
                        language = language,
                        key = "command.steal.response.invalid_image",
                        values = arrayOf(url)
                    )
                )
                event.thread.post(this)
            }
            return
        }

        event.guild.createEmote(name, icon).queue(
            {
                event.replyAsync {
                    type(CommandReply.Type.SUCCESS)
                    title(
                        translate(
                            language = language,
                            key = "command.steal.response.emoji_success",
                            values = arrayOf(it.asMention)
                        )
                    )
                    event.thread.post(this)
                    limiter.take(RateLimitedAction.EMOJI_CREATE)
                }
            },
            {
                event.replyAsync {
                    type(CommandReply.Type.EXCEPTION)
                    title(
                        translate(
                            language = language,
                            key = "command.steal.response.emoji_add_error",
                            values = arrayOf(url)
                        )
                    )
                    event.thread.post(this)
                }
            }
        )
    }
}