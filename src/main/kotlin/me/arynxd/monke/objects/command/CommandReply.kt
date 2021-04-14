package me.arynxd.monke.objects.command

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import me.arynxd.monke.util.DEFAULT_EMBED_COLOUR
import me.arynxd.monke.util.ERROR_EMBED_COLOUR
import me.arynxd.monke.util.SUCCESS_EMBED_COLOUR
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import java.time.Instant

class CommandReply(val event: CommandEvent) {
    private val embed = EmbedBuilder()
    private val mentions = mutableListOf<Message.MentionType>()
    private var type = Type.UNKNOWN

    fun send(callback: ((Message) -> Unit)) {
        if (type == Type.UNKNOWN) {
            throw IllegalStateException("Type is not set")
        }

        event.message.reply(embed.build())
            .mentionRepliedUser(false)
            .allowedMentions(mentions)
            .queue(callback)
    }

    fun send() {
        send {}
    }

    suspend fun await(): Message {
        if (type == Type.UNKNOWN) {
            throw IllegalStateException("Type is not set")
        }

        return event.message.reply(embed.build())
            .mentionRepliedUser(false)
            .allowedMentions(mentions)
            .await()
    }

    fun type(type: Type) {
        this.type = type
        embed.setColor(
            when (type) {
                Type.SUCCESS -> SUCCESS_EMBED_COLOUR
                Type.EXCEPTION -> ERROR_EMBED_COLOUR
                Type.INFORMATION -> DEFAULT_EMBED_COLOUR
                else -> throw IllegalArgumentException("Type $type is invalid")
            }
        )
    }

    fun field(title: String?, description: String?, inline: Boolean) =
        embed.addField(
            title?.take(MessageEmbed.TITLE_MAX_LENGTH),
            description?.take(MessageEmbed.VALUE_MAX_LENGTH),
            inline
        )

    fun fields(fields: Collection<MessageEmbed.Field>) = fields.forEach { field(it.name, it.value, it.isInline) }

    fun blankField(inline: Boolean) = embed.addBlankField(inline)

    fun title(title: String?) = embed.setTitle(title?.take(MessageEmbed.TITLE_MAX_LENGTH))

    fun description(description: String?) = embed.setDescription(description?.take(MessageEmbed.TEXT_MAX_LENGTH))

    fun timestamp(time: Instant = Instant.now()) = embed.setTimestamp(time)

    fun footer(text: String = event.user.asTag, url: String = event.user.effectiveAvatarUrl) =
        embed.setFooter(text.take(MessageEmbed.TEXT_MAX_LENGTH), url)

    fun thumbnail(url: String?) = embed.setThumbnail(url)

    fun image(url: String?) = embed.setImage(url)

    fun image(url: String?, size: Int?) = embed.setImage("$url?size=$size")

    fun mentions(vararg mentions: Message.MentionType) = this.mentions.addAll(mentions)

    fun chunks(parts: List<Any>) = parts.forEach { event.channel.sendMessage(it.toString()).queue() }

    fun build() = embed.build()

    companion object {
        fun sendError(message: Message, text: String) {
            val user = message.author
            message.reply(
                Embed(
                    description = text,
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
                    description = text,
                    color = SUCCESS_EMBED_COLOUR.rgb,
                    timestamp = Instant.now(),
                    footerText = user.name,
                    footerIcon = user.effectiveAvatarUrl
                )
            ).mentionRepliedUser(false).queue()
        }
    }

    enum class Type {
        UNKNOWN,
        INFORMATION,
        EXCEPTION,
        SUCCESS
    }
}