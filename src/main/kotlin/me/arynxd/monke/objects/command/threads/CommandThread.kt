package me.arynxd.monke.objects.command.threads

import me.arynxd.monke.handlers.CommandThreadHandler
import net.dv8tion.jda.api.entities.Message

/**
 * State holder for a thread, provides access to post messages to this thread
 */
class CommandThread(val messageId: Long, val responseIds: List<Long>) {
    val hasPosts: Boolean
        get() = responseIds.isNotEmpty()

    fun post(reply: CommandReply) {
        if (responseIds.isEmpty()) {
            reply.send() {
                reply.monke.handlers[CommandThreadHandler::class].put(
                    CommandThread(messageId, listOf(it.idLong))
                )
            }
        }
        else {
            reply.replace(responseIds) {
                reply.monke.handlers[CommandThreadHandler::class].put(
                    CommandThread(messageId, listOf(it.idLong))
                )
            }
        }
    }

    fun contains(id: Long) = responseIds.contains(id)

    suspend fun awaitPost(reply: CommandReply): Message {
        if (responseIds.isEmpty()) {
            val message = reply.await()
            reply.monke.handlers[CommandThreadHandler::class].put(
                CommandThread(messageId, listOf(message.idLong))
            )
            return message
        }
        else {
            val message = reply.replaceAwait(responseIds)
            reply.monke.handlers[CommandThreadHandler::class].put(
                CommandThread(messageId, listOf(message.idLong))
            )
            return message
        }
    }

    fun postChunks(reply: CommandReply, chunks: List<Any>) {
        reply.replaceChunks(responseIds, chunks) {
            val ids = (responseIds + it).distinct()
            val thread = CommandThread(messageId, ids)
            reply.monke.handlers[CommandThreadHandler::class].put(thread)
        }
    }
}