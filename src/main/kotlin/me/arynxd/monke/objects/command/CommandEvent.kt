package me.arynxd.monke.objects.command

import dev.minn.jda.ktx.Embed
import me.arynxd.monke.Monke
import me.arynxd.monke.events.GuildMessageEvent
import me.arynxd.monke.handlers.ConfigHandler
import me.arynxd.monke.handlers.GuildDataHandler
import me.arynxd.monke.objects.cache.GuildData
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.util.DEFAULT_EMBED_COLOUR
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import java.time.Instant

class CommandEvent(
    val event: GuildMessageEvent,
    val command: Command,
    val args: MutableList<Any>,
    val monke: Monke
) {

    val jda: JDA = event.jda
    val channel = event.channel
    val user: User = event.user
    val messageChannel: MessageChannel = event.channel
    val guild = event.guild
    val message = event.message
    val member = event.member
    val selfMember = event.guild.selfMember
    val guildIdLong = event.guild.idLong

    suspend fun reply(function: suspend CommandReply.() -> Unit) = function(CommandReply(this))

    fun replyAsync(function: CommandReply.() -> Unit) = function(CommandReply(this))

    fun isDeveloper(): Boolean = monke.handlers.get(ConfigHandler::class).config.developers.contains(user.id)

    @Suppress("UNCHECKED_CAST")
    fun <T> getArgument(indie: Int): T {
        if (!isArgumentPresent(indie) || indie < 0) {
            throw NoSuchElementException("Argument $indie does not exist")
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

    fun getDataCache(): GuildData = monke.handlers.get(GuildDataHandler::class).getCache(guildIdLong)
}