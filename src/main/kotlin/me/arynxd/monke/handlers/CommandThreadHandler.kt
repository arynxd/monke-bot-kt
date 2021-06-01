package me.arynxd.monke.handlers

import me.arynxd.monke.Monke
import me.arynxd.monke.objects.command.threads.CommandThread
import me.arynxd.monke.objects.handlers.Handler
import net.dv8tion.jda.api.entities.TextChannel
import java.util.concurrent.ConcurrentHashMap

class CommandThreadHandler(
    override val monke: Monke,
) : Handler() {
                                           //messageId - thread
    private val threads = ConcurrentHashMap<Long, CommandThread>()

    fun put(thread: CommandThread) {
        threads[thread.messageId] = thread
    }

    fun getOrNew(messageId: Long): CommandThread {
        val th = threads[messageId]?: CommandThread(messageId, listOf())
        threads[messageId] = th
        return th
    }

    fun delete(messageId: Long, channel: TextChannel) {
        val thread = threads[messageId]?: return
        val toDelete = thread.responseIds.map { it.toString() }.toMutableList()
        if (toDelete.size >= 2) {
            channel.deleteMessagesByIds(toDelete).queue()
        }
        else if (toDelete.size == 1) {
            channel.deleteMessageById(toDelete.first()).queue()
        }
    }
}