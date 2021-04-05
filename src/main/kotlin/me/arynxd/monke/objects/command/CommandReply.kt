package me.arynxd.monke.objects.command

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import me.arynxd.monke.util.DEFAULT_EMBED_COLOUR
import me.arynxd.monke.util.ERROR_EMBED_COLOUR
import me.arynxd.monke.util.SUCCESS_EMBED_COLOUR
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.time.Instant

class CommandReply(val event: CommandEvent) {
    private val embed = EmbedBuilder()
    private val mentions = mutableListOf<Message.MentionType>()
    private var type = Type.UNKNOWN

    fun send(callback: ((Message) -> Unit) = { }) {
        if (type == Type.UNKNOWN) {
            throw IllegalStateException("Type is not set")
        }
        event.message.reply(embed.build())
            .mentionRepliedUser(false)
            .allowedMentions(mentions)
            .queue(callback)
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
        embed.setColor(when(type) {
            Type.SUCCESS -> SUCCESS_EMBED_COLOUR
            Type.EXCEPTION -> ERROR_EMBED_COLOUR
            Type.INFORMATION -> DEFAULT_EMBED_COLOUR
            else -> throw IllegalArgumentException("Type $type is invalid")
        })
    }


    fun field(title: String?, description: String?, inline: Boolean) {
        embed.addField(
            title?.substring(0, title.length.coerceAtMost(MessageEmbed.TITLE_MAX_LENGTH)),
            description?.substring(0, description.length.coerceAtMost(MessageEmbed.VALUE_MAX_LENGTH)),
            inline
        )
    }

    fun fields(fields: Collection<MessageEmbed.Field>) {
        fields.forEach { field(it.name, it.value, it.isInline) }
    }

    fun blankField(inline: Boolean) {
        embed.addBlankField(inline)
    }

    fun title(title: String?) {
        if (title == null) {
            embed.setTitle(title)
            return
        }
        embed.setTitle(title.subSequence(0, title.length.coerceAtMost(MessageEmbed.TITLE_MAX_LENGTH)).toString())
    }

    fun description(description: String?) {
        if (description == null) {
            embed.setDescription(description)
            return
        }
        embed.setDescription(description.subSequence(0, description.length.coerceAtMost(MessageEmbed.TEXT_MAX_LENGTH)))
    }

    fun timestamp() {
        embed.setTimestamp(Instant.now())
    }

    fun footer(text: String = event.user.asTag, url: String = event.user.effectiveAvatarUrl) {
        embed.setFooter(text.substring(0, text.length.coerceAtMost(MessageEmbed.TEXT_MAX_LENGTH)), url)
    }

    fun thumbnail(url: String?) {
        embed.setThumbnail(url)
    }

    fun image(url: String?) {
        embed.setImage(url)
    }

    fun image(url: String?, size: Int?) {
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