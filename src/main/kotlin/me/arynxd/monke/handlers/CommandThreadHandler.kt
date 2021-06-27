package me.arynxd.monke.handlers

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.command.threads.CommandThread
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.util.IGNORE_UNKNOWN
import me.arynxd.monke.util.set
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent
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
            channel.deleteMessagesByIds(toDelete).queue(null, IGNORE_UNKNOWN)
        }
        else if (toDelete.size == 1) {
            channel.deleteMessageById(toDelete.first()).queue(null, IGNORE_UNKNOWN)
        }
    }

    override fun onGuildMessageDelete(event: GuildMessageDeleteEvent) {
        val id = event.messageIdLong
        val channel = event.channel
        delete(id, channel)

        val thread = threads.asMap().values.firstOrNull { it.contains(id) }?: return
        put(CommandThread(thread.messageId, thread.responseIds.takeWhile { it != id }))
        channel.deleteMessageById(id).queue(null, IGNORE_UNKNOWN) //Messages should be our own, so perm checks are not needed
    }
}
