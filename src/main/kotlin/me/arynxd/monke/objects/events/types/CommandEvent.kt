package me.arynxd.monke.objects.events.types

import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.ConfigHandler
import me.arynxd.monke.handlers.GuildDataHandler
import me.arynxd.monke.objects.cache.GuildData
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandReply
import me.arynxd.monke.objects.translation.Language
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.User

class CommandEvent(
    override val monke: Monke,
    val event: CommandPreprocessEvent,
    val command: Command,
    val args: MutableList<Any>
) : Event {
    val jda = event.jda
    val channel = event.channel
    val user = event.user
    val guild = event.guild
    val message = event.message
    val member = event.member
    val selfMember = event.guild.selfMember
    val guildIdLong = event.guild.idLong

    suspend fun reply(function: suspend CommandReply.() -> Unit) = function(CommandReply(this))

    fun replyAsync(function: CommandReply.() -> Unit) = function(CommandReply(this))

    fun isDeveloper(): Boolean = monke.handlers.get(ConfigHandler::class).config.developers.contains(user.id)

    @Suppress("UNCHECKED_CAST")
    fun <T> getArgument(indie: Int, default: T? = null): T {
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
    fun <T> getVararg(start: Int): MutableList<T> {
        if (start > args.size || start < 0) {
            throw NoSuchElementException("Variable argument $start does not exist")
        }

        return args.subList(start, args.size)
            .map { it as T }
            .toMutableList()
    }

    fun isArgumentPresent(indie: Int): Boolean = indie < args.size

    fun getPrefix(): String = getDataCache().prefix

    fun getLanguage(): Language = getDataCache().language

    fun getDataCache(): GuildData = monke.handlers.get(GuildDataHandler::class).getData(guildIdLong)
}