package me.arynxd.monke.commands.misc

import me.arynxd.monke.handlers.RateLimitHandler
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.argument.types.ArgumentURL
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.ratelimit.RateLimitedAction
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.util.getIcon
import me.arynxd.monke.util.sendError
import me.arynxd.monke.util.sendSuccess
import net.dv8tion.jda.api.Permission
import java.net.URL

val EMOJI_REGEX: Regex = Regex("([A-Z]|[a-z]|_){2,32}")

@Suppress("UNUSED")
class StealCommand : Command(
    name = "steal",
    description = "Steals an emote and adds it here.",
    category = CommandCategory.MISC,

    botPermissions = listOf(Permission.MANAGE_EMOTES),
    memberPermissions = listOf(Permission.MANAGE_EMOTES),

    cooldown = 180_000L, // 2 Minutes

    arguments = ArgumentConfiguration(listOf(
        ArgumentString(
            name = "name",
            description = "The new emoji name, ( must be A-Z or _ and 2 - 32 characters long ).",
            required = true,
            type = ArgumentType.REGULAR,
            condition = { it.matches(EMOJI_REGEX) }
        ),
        ArgumentURL(
            name = "emoji",
            description = "The emoji. Must be a valid image URL.",
            required = true,
            type = ArgumentType.REGULAR,
        )
    ))
) {

    override suspend fun run(event: CommandEvent) {
        val name = event.getArgument<String>(0)
        val url = event.getArgument<URL>(1)
        val language = event.getLanguage()
        val icon = getIcon(url)
        val limiter = event.monke.handlers.get(RateLimitHandler::class.java).getRateLimiter(event.guildIdLong)

        if (!limiter.canTake(RateLimitedAction.EMOJI_CREATE)) {
            sendError(event.message, TranslationHandler.getString(Language.EN_US, "command_error.rate_limited"))
            return
        }

        val couldNotLoad = TranslationHandler.getString(language, "command.steal.response.invalid_image", url)
        val couldNotAdd = TranslationHandler.getString(language, "command.steal.response.emoji_add_error", url)

        if (icon == null) {
            sendError(event.message, couldNotLoad)
            return
        }

        event.guild.createEmote(name, icon).queue({
            val success = TranslationHandler.getString(language, "command.steal.response.emoji_success", it.asMention)
            sendSuccess(event.message, success)
            limiter.take(RateLimitedAction.EMOJI_CREATE)
        }, {
            sendError(event.message, couldNotAdd)
        })
    }
}