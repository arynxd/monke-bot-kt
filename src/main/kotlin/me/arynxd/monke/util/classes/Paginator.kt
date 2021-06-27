package me.arynxd.monke.util.classes

import dev.minn.jda.ktx.await
import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.PaginationHandler
import me.arynxd.monke.objects.Emoji
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.util.IGNORE_UNKNOWN
import me.arynxd.monke.util.addReactions
import me.arynxd.monke.util.queue
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
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
            .sendMessageEmbeds(pages.first())
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
                .queue(null, IGNORE_UNKNOWN)

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
                getChannel().editMessageEmbedsById(sentMessage, pages.last())
                page = pages.size - 1
            }

            page >= pages.size -> {
                getChannel().editMessageEmbedsById(sentMessage, pages.first())
                page = 0
            }

            else -> getChannel().editMessageEmbedsById(sentMessage, pages[page])
        }

        awaitReaction()
    }

    fun delete() {
        getChannel().deleteMessageById(sentMessage).queue(null, IGNORE_UNKNOWN)
    }
}

fun MessageChannel.sendPaginator(monke: Monke, userId: Long, messageId: Long, vararg embeds: MessageEmbed) {
    val paginator = Paginator(
        monke = monke,
        authorId = userId,
        channelId = this.idLong,
        messageId = messageId,
        pages = embeds.toList()
    )

    monke.handlers[PaginationHandler::class]
        .addPaginator(paginator)
}

fun MessageChannel.sendPaginator(event: CommandEvent, vararg embeds: MessageEmbed) {
    sendPaginator(event.monke, event.user.idLong, event.messageIdLong, *embeds)
}
fun MessageChannel.sendPaginator(monke: Monke, userId: Long, messageId: Long, embeds: Collection<MessageEmbed>) {
    sendPaginator(monke, userId, messageId, *embeds.toTypedArray())
}

fun MessageChannel.sendPaginator(event: CommandEvent, embeds: Collection<MessageEmbed>) {
    sendPaginator(event, *embeds.toTypedArray())
}


