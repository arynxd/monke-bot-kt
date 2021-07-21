package me.arynxd.monke.util.classes

import me.arynxd.monke.Monke
import me.arynxd.monke.objects.Emoji
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.objects.command.threads.CommandThread
import me.arynxd.monke.util.addReactions
import me.arynxd.monke.util.queue
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.internal.utils.Checks
import java.util.concurrent.TimeUnit

class ThreadPaginator(
    val thread: CommandThread,
    monke: Monke,
    authorId: Long,
    channelId: Long,
    pages: List<MessageEmbed>,
    timeout: Long = 1,
    timeoutUnit: TimeUnit = TimeUnit.MINUTES
) : Paginator(
    messageId = thread.messageId,
    pages = pages,
    monke = monke,
    authorId = authorId,
    channelId = channelId,
    timeout = timeout,
    timeoutUnit = timeoutUnit
) {
    override suspend fun paginate() {
        Checks.notEmpty(pages, "Pages")
        val channel = getChannel()
        require(channel is TextChannel) { "Channel MUST be a TextChannel for a ThreadPaginator" }

        val reply = CommandReply.fromEmbed(
            embed = pages.first(),
            messageId = messageId,
            channel = getChannel() as TextChannel,
            monke = monke
        )

        reply.type(CommandReply.Type.INFORMATION)

        val message = thread.awaitPost(reply)

        message.addReactions(
            Emoji.ARROW_LEFT.asReaction,
            Emoji.ARROW_RIGHT.asReaction,
            Emoji.WASTE_BASKET.asReaction
        ).queue()

        super.sentMessage = message.idLong // We could override and add CommandThread#post, but for now this will be fine
        awaitReaction()
    }
}