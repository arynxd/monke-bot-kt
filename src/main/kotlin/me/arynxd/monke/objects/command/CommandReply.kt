package me.arynxd.monke.objects.command

import dev.minn.jda.ktx.await
import me.arynxd.monke.util.DEFAULT_EMBED_COLOUR
import me.arynxd.monke.util.ERROR_EMBED_COLOUR
import me.arynxd.monke.util.SUCCESS_EMBED_COLOUR
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import java.time.Instant

class CommandReply(val event: CommandEvent) {
    private val embed = EmbedBuilder()
    private val mentions = mutableListOf<Message.MentionType>()

    fun send(callback: ((Message) -> Unit) = { }) {
        event.message.reply(embed.build())
            .mentionRepliedUser(false)
            .allowedMentions(mentions)
            .queue()
    }

    suspend fun await(): Message {
        return event.message.reply(embed.build())
            .mentionRepliedUser(false)
            .allowedMentions(mentions)
            .await()
    }

    fun exception() {
        embed.setColor(ERROR_EMBED_COLOUR)
    }

    fun success() {
        embed.setColor(SUCCESS_EMBED_COLOUR)
    }

    fun information() {
        embed.setColor(DEFAULT_EMBED_COLOUR)
    }

    fun field(title: String, description: String, inline: Boolean = false) {
        embed.addField(title, description, inline)
    }

    fun blankField(inline: Boolean = false) {
        embed.addBlankField(inline)
    }

    fun title(title: String) {
        embed.setTitle(title)
    }

    fun description(description: String) {
        embed.setDescription(description)
    }

    fun timestamp() {
        embed.setTimestamp(Instant.now())
    }

    fun footerIcon(text: String = event.user.asTag, url: String = event.user.effectiveAvatarUrl) {
        embed.setFooter(text, url)
    }

    fun thumbnail(url: String) {
        embed.setThumbnail(url)
    }

    fun image(url: String) {
        embed.setImage(url)
    }

    fun image(url: String, size: Int) {
        embed.setImage("$url?size=$size")
    }

    fun mentions(vararg mentions: Message.MentionType) {
        this.mentions.addAll(mentions)
    }

    fun chunks(parts: List<Any>) {
        for (part in parts) {
            event.channel.sendMessage(part.toString()).queue()
        }
    }

    operator fun invoke(body: CommandReply.() -> Unit) {
        body()
    }
}