package me.arynxd.monke.handlers

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.command.threads.CommandThread
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.util.ignoreUnknown
import net.dv8tion.jda.api.entities.TextChannel
import java.util.concurrent.TimeUnit

class CommandThreadHandler(
    override val monke: Monke,
) : Handler() {                      //messageId - thread
    private val threads: LoadingCache<Long, CommandThread> =
        Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build { CommandThread(it, listOf()) }

    fun put(thread: CommandThread) {
        threads[thread.messageId] = thread
    }

    fun getOrNew(messageId: Long): CommandThread {
        val thr = threads[messageId] ?: CommandThread(messageId, listOf())
        threads[messageId] = thr
        return thr
    }

    fun delete(messageId: Long, channel: TextChannel) {
        val thread = threads[messageId] ?: return
        val toDelete = thread.responseIds.map { it.toString() }.toMutableList()
        if (toDelete.size >= 2) {
            channel.deleteMessagesByIds(toDelete).queue(null, ignoreUnknown())
        }
        else if (toDelete.size == 1) {
            channel.deleteMessageById(toDelete.first()).queue(null, ignoreUnknown())
        }
    }
}

private operator fun <K : Any, V : Any> LoadingCache<K, V>.set(messageId: K, value: V) {
    this.put(messageId, value)
}
