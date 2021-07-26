package me.arynxd.monke.objects.command

import me.arynxd.monke.launch.Monke
import me.arynxd.monke.events.CommandPreprocessEvent
import me.arynxd.monke.handlers.CommandThreadHandler
import me.arynxd.monke.handlers.ConfigHandler
import me.arynxd.monke.handlers.GuildDataHandler
import me.arynxd.monke.objects.cache.GuildData
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.objects.command.threads.CommandThread
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.util.subList

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
    val messageIdLong = message.idLong
    val member = event.member
    val user = event.user
    val jda = event.jda

    /** Gets refreshed with every call **/

    /**
     * This is an IMMUTABLE object, its state is not updated when replies are posted to it.
     *
     * Take care when sending multiple replies in 1 command cycle.
     */
    val thread: CommandThread
        get() = monke.handlers[CommandThreadHandler::class].getOrNew(message.idLong)

    /**
     * This is a one use object, hence it is refreshed upon every call to it
     */
    val reply: CommandReply
        get() = CommandReply(this)

    /** Lazy loaded for performance **/
    val isDeveloper: Boolean
        get() = monke.handlers[ConfigHandler::class].config.developers.contains(user.id)

    val dataCache: GuildData
        get() = monke.handlers[GuildDataHandler::class].getData(guildIdLong)

    val prefix: String
        get() = dataCache.prefix

    val language: Language
        get() = dataCache.language

    inline fun <reified T> argument(indie: Int, default: T? = null): T {
        if (indie < 0) {
            throw NoSuchElementException("Argument $indie does not exist")
        }

        if (!isArgumentPresent(indie)) {
            if (default == null) {
                throw NoSuchElementException("Argument $indie does not exist")
            }
            return default
        }

        return args[indie] as? T
            ?: throw IllegalStateException("Argument ${args[indie]} was not of type ${T::class.simpleName}")
    }

    inline fun <reified T> vararg(start: Int): MutableList<T> {
        if (start > args.size || start < 0) {
            throw NoSuchElementException("Variable argument $start does not exist")
        }

        return args.subList(start)
            .map { it as? T ?: throw IllegalStateException("Argument $it was not of type ${T::class.simpleName}") }
            .toMutableList()
    }

    suspend fun reply(reply: suspend CommandReply.() -> Unit): CommandReply {
        val repl = this.reply
        reply(repl)
        return repl
    }

    fun replyAsync(reply: CommandReply.() -> Unit): CommandReply {
        val repl = this.reply
        reply(repl)
        return repl
    }

    fun isArgumentPresent(indie: Int) = indie < args.size
}