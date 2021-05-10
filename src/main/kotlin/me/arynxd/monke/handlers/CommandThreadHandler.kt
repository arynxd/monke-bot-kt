package me.arynxd.monke.handlers

import me.arynxd.monke.Monke
import me.arynxd.monke.objects.command.threads.CommandThread
import me.arynxd.monke.objects.handlers.Handler
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
}