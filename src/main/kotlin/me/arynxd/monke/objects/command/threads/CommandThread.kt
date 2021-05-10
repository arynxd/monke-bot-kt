package me.arynxd.monke.objects.command.threads

import me.arynxd.monke.handlers.CommandThreadHandler
import me.arynxd.monke.objects.command.CommandEvent

class CommandThread(val messageId: Long, val responseIds: List<Long>) {
    fun post(reply: CommandReply) {
        if (responseIds.isEmpty()) {
            reply.send() {
                reply.event.monke.handlers[CommandThreadHandler::class].put(
                    CommandThread(messageId, listOf(it.idLong))
                )
            }
        }
        else {
            reply.replace(responseIds) {
                reply.event.monke.handlers[CommandThreadHandler::class].put(
                    CommandThread(messageId, listOf(it.idLong))
                )
            }
        }
    }

    fun postChunks(reply: CommandReply, chunks: List<Any>) {
        reply.replaceChunks(responseIds, chunks) {
            reply.event.monke.handlers[CommandThreadHandler::class].put(
                CommandThread(messageId, it)
            )
        }
    }
}