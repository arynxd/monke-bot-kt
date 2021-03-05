package me.arynxd.monke.util

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.Emoji
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import java.awt.Color
import java.time.Instant

val DEFAULT_EMBED_COLOUR: Color = Color.decode("#C29957")
val ERROR_EMBED_COLOUR: Color = Color.decode("#B95F67")
val SUCCESS_EMBED_COLOUR: Color = Color.decode("#66B393")

fun sendError(message: Message, text: String) {
    val user = message.author
    message.reply(
        Embed(
            description = Emoji.ERROR.getAsChat() + text,
            color = ERROR_EMBED_COLOUR.rgb,
            footerText = user.name,
            timestamp = Instant.now(),
            footerIcon = user.effectiveAvatarUrl
        )
    ).mentionRepliedUser(false).queue()
}

fun sendSuccess(message: Message, text: String) {
    val user = message.author
    message.reply(
        Embed(
            description = Emoji.SUCCESS.getAsChat() + text,
            color = SUCCESS_EMBED_COLOUR.rgb,
            timestamp = Instant.now(),
            footerText = user.name,
            footerIcon = user.effectiveAvatarUrl
        )
    ).mentionRepliedUser(false).queue()
}

fun sendSuccess(channel: MessageChannel, user: User, text: String) {
    channel.sendMessage(
        Embed(
            description = Emoji.SUCCESS.getAsChat() + text,
            color = SUCCESS_EMBED_COLOUR.rgb,
            timestamp = Instant.now(),
            footerText = user.name,
            footerIcon = user.effectiveAvatarUrl
        )
    ).queue()
}

fun sendError(channel: MessageChannel, user: User, text: String) {
    channel.sendMessage(
        Embed(
            description = Emoji.ERROR.getAsChat() + text,
            color = ERROR_EMBED_COLOUR.rgb,
            timestamp = Instant.now(),
            footerText = "${user.asTag} | ",
            footerIcon = user.effectiveAvatarUrl
        )
    ).queue()
}

suspend fun awaitConfirmation(message: Message, user: User, monke: Monke): Boolean? {
    val userId = user.idLong
    message.addReaction(Emoji.GREEN_TICK.getAsReaction()).queue()
    message.addReaction(Emoji.GREEN_CROSS.getAsReaction()).queue()
    return try {
        withTimeout(10_000) {
            val event =
                monke.jda.await<GuildMessageReactionAddEvent> { it.userIdLong == userId && it.messageIdLong == message.idLong }

            if (event.reactionEmote.isEmote) {
                return@withTimeout null
            }

            return@withTimeout when (event.reactionEmote.emoji) {
                Emoji.GREEN_TICK.getAsReaction() -> true
                Emoji.GREEN_CROSS.getAsReaction() -> false
                else -> null
            }
        }
    } catch (exception: TimeoutCancellationException) {
        null
    }
}