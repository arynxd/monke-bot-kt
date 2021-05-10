package me.arynxd.monke.objects.command

import me.arynxd.monke.Monke
import me.arynxd.monke.events.CommandPreprocessEvent
import me.arynxd.monke.handlers.CommandThreadHandler
import me.arynxd.monke.handlers.ConfigHandler
import me.arynxd.monke.handlers.GuildDataHandler
import me.arynxd.monke.objects.cache.GuildData
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.objects.translation.Language

class CommandEvent(
    val monke: Monke,
    val args: MutableList<Any>,
    val command: Command,

    val event: CommandPreprocessEvent
) {
    val channel = event.channel
    val guild = event.guild
    val selfMember = guild.selfMember
    val guildIdLong = guild.idLong
    val message = event.message
    val member = event.member
    val user = event.user
    val jda = event.jda
    val thread = monke.handlers[CommandThreadHandler::class].getOrNew(message.idLong)

    @Suppress("UNCHECKED_CAST")
    fun <T> argument(indie: Int, default: T? = null): T {
        if (indie < 0) {
            throw NoSuchElementException("Argument $indie does not exist")
        }

        if (!isArgumentPresent(indie)) {
            if (default == null) {
                throw NoSuchElementException("Argument $indie does not exist")
            }
            return default
        }

        return args[indie] as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> vararg(start: Int): MutableList<T> {
        if (start > args.size || start < 0) {
            throw NoSuchElementException("Variable argument $start does not exist")
        }

        return args.subList(start, args.size)
            .map { it as T }
            .toMutableList()
    }

    suspend fun reply(reply: suspend CommandReply.() -> Unit): CommandReply {
        val repl = CommandReply(this)
        reply(repl)
        return repl
    }

    fun replyAsync(reply: CommandReply.() -> Unit): CommandReply {
        val repl = CommandReply(this)
        reply(repl)
        return repl
    }

    fun isDeveloper(): Boolean = monke.handlers[ConfigHandler::class].config.developers.contains(user.id)

    fun isArgumentPresent(indie: Int): Boolean = indie < args.size

    fun prefix(): String = dataCache().prefix

    fun language(): Language = dataCache().language

    fun dataCache(): GuildData = monke.handlers[GuildDataHandler::class].getData(guildIdLong)
}