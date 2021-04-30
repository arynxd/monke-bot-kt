package me.arynxd.monke.objects.events.types.command

import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.GuildDataHandler
import me.arynxd.monke.objects.cache.GuildData
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.events.types.BaseEvent
import me.arynxd.monke.objects.translation.Language

class CommandEvent(
    override val monke: Monke,
    val args: MutableList<Any>,
    val command: Command,

    event: CommandPreprocessEvent
) : BaseEvent, GenericCommandEvent(
    monke = monke,

    jda = event.jda,
    channel = event.channel,
    user = event.user,
    member = event.member,
    guild = event.guild,
    message = event.message
) {

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

    fun isArgumentPresent(indie: Int): Boolean = indie < args.size

    fun prefix(): String = dataCache().prefix

    fun language(): Language = dataCache().language

    fun dataCache(): GuildData = monke.handlers.get(GuildDataHandler::class).getData(guildIdLong)
}