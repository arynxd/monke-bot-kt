package me.arynxd.monke.util.classes

import dev.minn.jda.ktx.await
import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.translateInternal
import me.arynxd.monke.objects.Emoji
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.internal.utils.Checks
import java.lang.IllegalArgumentException

class Paginator(
    val monke: Monke,
    val message: Message,
    val pages: List<MessageEmbed>,
) {
    private val author = message.author
    private lateinit var sentMessage: Message
    private var page = 0

    var lastUsed = System.currentTimeMillis()

    suspend fun paginate() {
        Checks.notEmpty(pages, "Pages")

        sentMessage = message.reply(pages.first()).mentionRepliedUser(false).await()
        sentMessage.addReaction(Emoji.ARROW_LEFT.asReaction).queue()
        sentMessage.addReaction(Emoji.ARROW_RIGHT.asReaction).queue()
        sentMessage.addReaction(Emoji.WASTE_BASKET.asReaction).queue()

        awaitReaction()
    }

    private suspend fun awaitReaction() {
        lastUsed = System.currentTimeMillis()
        val event = monke.jda.await<GuildMessageReactionAddEvent> {
            it.userIdLong == author.idLong && it.messageIdLong == sentMessage.idLong
        }

        if (event.reactionEmote.isEmote) {
            event.reaction.removeReaction(author).queue()
            awaitReaction()
            return
        }

        when (event.reactionEmote.emoji) {
            Emoji.ARROW_LEFT.asReaction -> {
                page--
                event.reaction.removeReaction(author).queue()
                changePage()
            }

            Emoji.ARROW_RIGHT.asReaction -> {
                page++
                event.reaction.removeReaction(author).queue()
                changePage()
            }

            Emoji.WASTE_BASKET.asReaction -> sentMessage.delete().queue()

            else -> event.reaction.removeReaction(author).queue()
        }
    }

    private suspend fun changePage() {
        when {
            page < 0 -> {
                sentMessage.editMessage(pages.last()).queue()
                page = pages.size - 1
            }

            page >= pages.size -> {
                sentMessage.editMessage(pages.first()).queue()
                page = 0
            }

            else -> sentMessage.editMessage(pages[page]).queue()
        }

        awaitReaction()
    }

    fun delete() {
        sentMessage.delete().queue(null, ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE))
    }
}
