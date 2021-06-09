package me.arynxd.monke.util.classes

import dev.minn.jda.ktx.await
import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.PaginationHandler
import me.arynxd.monke.objects.Emoji
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.util.addReactions
import me.arynxd.monke.util.ignoreUnknown
import me.arynxd.monke.util.queue
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.internal.utils.Checks

class Paginator(
    val monke: Monke,
    val authorId: Long,
    val channelId: Long,
    val messageId: Long,
    val pages: List<MessageEmbed>,
) {
    private var sentMessage = -1L
    private var page = 0

    var lastUsed = System.currentTimeMillis()

    suspend fun paginate() {
        Checks.notEmpty(pages, "Pages")

        val message = getChannel()
            .sendMessage(pages.first())
            .referenceById(messageId)
            .mentionRepliedUser(false)
            .await()

        message.addReactions(
            Emoji.ARROW_LEFT.asReaction,
            Emoji.ARROW_RIGHT.asReaction,
            Emoji.WASTE_BASKET.asReaction
        ).queue()

        sentMessage = message.idLong
        awaitReaction()
    }

    private suspend fun awaitReaction() {
        lastUsed = System.currentTimeMillis()
        val event = monke.jda.await<GuildMessageReactionAddEvent> {
            it.userIdLong == authorId && it.messageIdLong == sentMessage
        }
        val user = event.user

        if (event.reactionEmote.isEmote) {
            event.reaction.removeReaction(user).queue()
            awaitReaction()
            return
        }

        when (event.reactionEmote.emoji) {
            Emoji.ARROW_LEFT.asReaction -> {
                page--
                event.reaction.removeReaction(user).queue()
                changePage()
            }

            Emoji.ARROW_RIGHT.asReaction -> {
                page++
                event.reaction.removeReaction(user).queue()
                changePage()
            }

            Emoji.WASTE_BASKET.asReaction -> getChannel()
                .deleteMessageById(sentMessage)
                .queue(null, ignoreUnknown())

            else -> event.reaction.removeReaction(user).queue()
        }
    }

    private fun getChannel(): MessageChannel {
        return monke.jda.getTextChannelById(channelId)
            ?: monke.jda.getPrivateChannelById(channelId)
            ?: throw IllegalStateException("Channel $channelId does not exist")
    }

    private suspend fun changePage() {
        when {
            page < 0 -> {
                getChannel().editMessageById(sentMessage, pages.last())
                page = pages.size - 1
            }

            page >= pages.size -> {
                getChannel().editMessageById(sentMessage, pages.first())
                page = 0
            }

            else -> getChannel().editMessageById(sentMessage, pages[page])
        }

        awaitReaction()
    }

    fun delete() {
        getChannel().deleteMessageById(sentMessage).queue(null, ignoreUnknown())
    }
}

fun MessageChannel.sendPaginator(event: CommandEvent, vararg embeds: MessageEmbed) {
    val paginator = Paginator(
        monke = event.monke,
        authorId = event.user.idLong,
        channelId = event.channel.idLong,
        messageId = event.message.idLong,
        pages = embeds.toList()
    )

    event.monke.handlers[PaginationHandler::class]
        .addPaginator(paginator)
}

fun MessageChannel.sendPaginator(event: CommandEvent, embeds: Collection<MessageEmbed>) {
    this.sendPaginator(event, *embeds.toTypedArray())
}


