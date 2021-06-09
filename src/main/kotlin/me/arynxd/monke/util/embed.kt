package me.arynxd.monke.util

import dev.minn.jda.ktx.await
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import me.arynxd.monke.Monke
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageType
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.ButtonStyle
import java.awt.Color

val DEFAULT_EMBED_COLOUR: Color = Color.decode("#C29957")
val ERROR_EMBED_COLOUR: Color = Color.decode("#B95F67")
val SUCCESS_EMBED_COLOUR: Color = Color.decode("#66B393")

val SUCCESS_EMOJI = Emoji.fromUnicode(me.arynxd.monke.objects.Emoji.GREEN_TICK.asReaction)
val FAILURE_EMOJI = Emoji.fromUnicode(me.arynxd.monke.objects.Emoji.GREEN_CROSS.asReaction)
val WASTE_BASKET_EMOJI = Emoji.fromUnicode(me.arynxd.monke.objects.Emoji.WASTE_BASKET.asReaction)

suspend fun awaitConfirmation(message: Message, user: User, monke: Monke): Boolean? {
    val userId = user.idLong
    message.editMessage(message.embeds[0]) //TODO: change this once https://github.com/DV8FromTheWorld/JDA/issues/1654 is resolved
        .setActionRow(
            Button.of(ButtonStyle.PRIMARY, "success", SUCCESS_EMOJI),
            Button.of(ButtonStyle.PRIMARY, "failure", FAILURE_EMOJI),
            Button.of(ButtonStyle.DANGER, "cancel", WASTE_BASKET_EMOJI),
        ).queue()

    return try {
        withTimeout(10_000) {
            val event = monke.jda.await<ButtonClickEvent> {
                    it.user.idLong == userId && it.messageIdLong == message.idLong
                }

            return@withTimeout when (event.componentId) {
                "success" -> true
                "failure" -> false
                "cancel" -> null
                else -> null
            }
        }
    }
    catch (exception: TimeoutCancellationException) {
        null
    }
}