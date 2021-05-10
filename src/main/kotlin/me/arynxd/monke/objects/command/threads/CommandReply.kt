package me.arynxd.monke.objects.command.threads

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.util.DEFAULT_EMBED_COLOUR
import me.arynxd.monke.util.ERROR_EMBED_COLOUR
import me.arynxd.monke.util.SUCCESS_EMBED_COLOUR
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import java.time.Instant

const val TYPE_NOT_SET_ERROR = "Type is not set"

class CommandReply(val event: CommandEvent) {
    private val embed = EmbedBuilder()
    private val mentions = mutableListOf<Message.MentionType>()
    private var type = Type.UNKNOWN

    fun replace(messageIds: List<Long>, callback: ((Message) -> Unit) = {}) {
        if (type == Type.UNKNOWN) {
            throw IllegalStateException(TYPE_NOT_SET_ERROR)
        }

        if (messageIds.isEmpty()) {
            throw IllegalArgumentException("IDs were empty")
        }

        event.channel.editMessageById(messageIds[0], embed.build())
            .mentionRepliedUser(false)
            .allowedMentions(mentions)
            .queue(callback)

        if (messageIds.size >= 2) {
            event.channel.deleteMessagesByIds(messageIds.subList(1, messageIds.size).map { it.toString() }).queue()
        }
        else {
            event.channel.deleteMessageById(messageIds[1]).queue()
        }
    }

    fun send(callback: ((Message) -> Unit) = {}) {
        if (type == Type.UNKNOWN) {
            throw IllegalStateException(TYPE_NOT_SET_ERROR)
        }

        event.message.reply(embed.build())
            .mentionRepliedUser(false)
            .allowedMentions(mentions)
            .queue(callback)
    }

    suspend fun await(): Message {
        if (type == Type.UNKNOWN) {
            throw IllegalStateException(TYPE_NOT_SET_ERROR)
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

    fun chunks(parts: List<Any>) = parts.forEach {
        event.message.reply(it.toString())
            .mentionRepliedUser(false)
            .allowedMentions(mentions)
            .queue()
    }

    fun replaceChunks(messageIds: List<Long>, parts: List<Any>, callback: ((List<Long>) -> Unit) = {}) {
        if (type == Type.UNKNOWN) {
            throw IllegalStateException(TYPE_NOT_SET_ERROR)
        }

        if (messageIds.isEmpty()) {
            throw IllegalArgumentException("IDs were empty")
        }

        val target = parts.size
        val ids = mutableListOf<Long>()
        var idCount = 0

        if (messageIds.size > parts.size) {
            val sub = messageIds.subList(0, parts.size)

            for ((i, id) in sub.withIndex()) {
                event.channel.editMessageById(id, parts[i].toString())
                    .mentionRepliedUser(false)
                    .allowedMentions(mentions)
                    .queue() {
                        if (idCount >= target) {
                            ids.add(it.idLong)
                            idCount++
                            callback(ids)
                        }
                    }
            }

            if (messageIds.size >= 2) {
                event.channel.deleteMessagesByIds(messageIds.subList(1, messageIds.size).map { it.toString() }).queue()
            }
            else {
                event.channel.deleteMessageById(messageIds[0]).queue()
            }
        }
        else {
            for ((i, id) in messageIds.withIndex()) {
                event.channel.editMessageById(id, parts[i].toString())
                    .mentionRepliedUser(false)
                    .allowedMentions(mentions)
                    .queue() {
                        if (idCount >= target) {
                            ids.add(it.idLong)
                            idCount++
                            callback(ids)
                        }
                    }

                event.channel.retrieveMessageById(id).queue() {m ->
                    m.editMessage(parts[i].toString())
                        .mentionRepliedUser(false)
                        .allowedMentions(mentions)
                        .queue() {
                            if (idCount >= target) {
                                ids.add(it.idLong)
                                idCount++
                                callback(ids)
                            }
                        }

                    m.suppressEmbeds(true).queue()
                }
            }
            for (part in parts.subList(messageIds.size, parts.size)) {
                event.channel.sendMessage(part.toString())
                    .mentionRepliedUser(false)
                    .allowedMentions(mentions)
                    .queue() {
                        if (idCount >= target) {
                            ids.add(it.idLong)
                            idCount++
                            callback(ids)
                        }
                    }
            }
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