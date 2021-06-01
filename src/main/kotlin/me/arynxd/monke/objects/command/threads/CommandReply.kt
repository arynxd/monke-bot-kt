package me.arynxd.monke.objects.command.threads

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.util.DEFAULT_EMBED_COLOUR
import me.arynxd.monke.util.ERROR_EMBED_COLOUR
import me.arynxd.monke.util.SUCCESS_EMBED_COLOUR
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import java.time.Instant

const val TYPE_NOT_SET_ERROR = "Type is not set"

class CommandReply(val message: Message, val channel: TextChannel, val user: User, val monke: Monke) {
    constructor(event: CommandEvent) : this(
        message = event.message,
        channel = event.channel,
        user = event.user,
        monke = event.monke
    )

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

        channel.editMessageById(messageIds[0], embed.build())
            .mentionRepliedUser(false)
            .allowedMentions(mentions)
            .override(true)
            .queue(callback)

        doDelete(messageIds)
    }

    suspend fun replaceAwait(messageIds: List<Long>): Message {
        if (type == Type.UNKNOWN) {
            throw IllegalStateException(TYPE_NOT_SET_ERROR)
        }

        if (messageIds.isEmpty()) {
            throw IllegalArgumentException("IDs were empty")
        }

        val message = channel.editMessageById(messageIds[0], embed.build())
            .mentionRepliedUser(false)
            .allowedMentions(mentions)
            .override(true)
            .await()

        doDelete(messageIds)
        return message
    }

    fun send(callback: ((Message) -> Unit) = {}) {
        if (type == Type.UNKNOWN) {
            throw IllegalStateException(TYPE_NOT_SET_ERROR)
        }

        message.reply(embed.build())
            .mentionRepliedUser(false)
            .allowedMentions(mentions)
            .queue(callback)
    }

    suspend fun await(): Message {
        if (type == Type.UNKNOWN) {
            throw IllegalStateException(TYPE_NOT_SET_ERROR)
        }

        return message.reply(embed.build())
            .mentionRepliedUser(false)
            .allowedMentions(mentions)
            .override(true)
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

    fun footer(text: String = user.asTag, url: String = user.effectiveAvatarUrl) =
        embed.setFooter(text.take(MessageEmbed.TEXT_MAX_LENGTH), url)

    fun thumbnail(url: String?) = embed.setThumbnail(url)

    fun image(url: String?) = embed.setImage(url)

    fun image(url: String?, size: Int?) = embed.setImage("$url?size=$size")

    fun mentions(vararg mentions: Message.MentionType) = this.mentions.addAll(mentions)

    fun chunks(parts: List<Any>) = parts.forEach {
       message.reply(it.toString())
            .mentionRepliedUser(false)
            .override(true )
            .allowedMentions(mentions)
            .queue()
    }

    fun replaceChunks(messageIds: List<Long>, parts: List<Any>, callback: ((List<Long>) -> Unit) = {}) {
        if (type == Type.UNKNOWN) {
            throw IllegalStateException(TYPE_NOT_SET_ERROR)
        }

        val target = parts.size
        val gatheredIds = mutableListOf<Long>()
        var idCount = 0

        if (messageIds.size > parts.size) {
            doEdit(messageIds.subList(0, parts.size), parts) {
                idCount++
                if (idCount >= target) {
                    gatheredIds.add(it.idLong)
                    callback(gatheredIds)
                    doDelete(gatheredIds)
                }
            }
            doDelete(messageIds.subList(parts.size, messageIds.size))
        }
        else {
            doEdit(messageIds, parts) {
                idCount++
                if (idCount >= target) {
                    gatheredIds.add(it.idLong)
                    callback(gatheredIds)
                    doDelete(gatheredIds)
                }
            }
            doReply(parts.subList(messageIds.size, parts.size)) {
                idCount++
                if (idCount >= target) {
                    gatheredIds.add(it.idLong)
                    callback(gatheredIds)
                    doDelete(gatheredIds)
                }
            }
        }
    }

    private fun doReply(parts: List<Any>, callback: (Message) -> Unit) {
        for (part in parts) {
            message.reply(part.toString())
                .mentionRepliedUser(false)
                .allowedMentions(mentions)
                .override(true)
                .queue {
                    callback(it)
                }
        }
    }

    private fun doEdit(ids: List<Long>, parts: List<Any>, callback: (Message) -> Unit) {
        for ((i, id) in ids.withIndex()) {
            channel.editMessageById(id, parts[i].toString())
                .mentionRepliedUser(false)
                .allowedMentions(mentions)
                .override(true)
                .queue {
                    callback(it)
                }
        }
    }

    private fun doDelete(sentIds: List<Long>) {
        val toDelete = sentIds.map { it.toString() }.toMutableList()
        toDelete.removeAt(0) //Leave 1 reply
        if (toDelete.size >= 2) {
            channel.deleteMessagesByIds(toDelete.subList(1, toDelete.size)).queue()
        }
        else if (toDelete.size >= 1) {
            channel.deleteMessageById(toDelete.first()).queue()
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