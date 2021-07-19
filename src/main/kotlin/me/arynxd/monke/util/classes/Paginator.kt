package me.arynxd.monke.util.classes

import dev.minn.jda.ktx.await
import kotlinx.coroutines.withTimeoutOrNull
import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.PaginationHandler
import me.arynxd.monke.objects.Emoji
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.util.IGNORE_UNKNOWN
import me.arynxd.monke.util.addReactions
import me.arynxd.monke.util.queue
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.internal.utils.Checks
import java.util.concurrent.TimeUnit

class Paginator(
    val monke: Monke,
    val authorId: Long,
    val channelId: Long,
    val messageId: Long,
    val pages: List<MessageEmbed>,
    timeout: Long = 1,
    timeoutUnit: TimeUnit = TimeUnit.MINUTES
) {
    private var sentMessage = -1L
    private var page = 0

    val timeout = timeoutUnit.toMillis(timeout)

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
        val event = withTimeoutOrNull(timeout) {
            monke.jda.await<MessageReactionAddEvent> {
                it.userIdLong == authorId && it.messageIdLong == sentMessage
            }
        }

        if (event == null) {
            delete()
            return
        }

        val user = event.retrieveUser().await()

        if (event.reactionEmote.isEmote) {
            removeReaction(event, user)
            awaitReaction()
            return
        }

        when (event.reactionEmote.emoji) {
            Emoji.ARROW_LEFT.asReaction -> {
                page--
                removeReaction(event, user)
                changePage()
            }

            Emoji.ARROW_RIGHT.asReaction -> {
                page++
                removeReaction(event, user)
                changePage()
            }

            Emoji.WASTE_BASKET.asReaction -> delete()

            else -> removeReaction(event, user)
        }
    }

    private fun removeReaction(event: MessageReactionAddEvent, user: User) {
        if (event.isFromGuild) {
            val guild = event.guild
            if (guild.selfMember.hasPermission(event.textChannel, Permission.MESSAGE_MANAGE)) {
                event.reaction.removeReaction(user).queue()
            }
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
                getChannel().editMessageEmbedsById(sentMessage, pages.last()).queue()
                page = pages.size - 1
            }

            page >= pages.size -> {
                getChannel().editMessageEmbedsById(sentMessage, pages.first()).queue()
                page = 0
            }

            else -> getChannel().editMessageEmbedsById(sentMessage, pages[page]).queue()
        }

        awaitReaction()
    }

    fun delete() {
        val channel = getChannel()
        if (channel is PrivateChannel) {
            return
        }
        else if (channel is TextChannel) {
            channel.deleteMessageById(sentMessage) // This shouldnt raise perm errors because its our own message
                .queue(null, IGNORE_UNKNOWN)
        }
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


