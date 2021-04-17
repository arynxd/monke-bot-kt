package me.arynxd.monke.objects.events.types.command

import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.ConfigHandler
import me.arynxd.monke.objects.command.CommandReply
import me.arynxd.monke.objects.events.types.BaseEvent
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*

open class GenericCommandEvent(
    override val monke: Monke,

    val jda: JDA,
    val channel: TextChannel,
    val user: User,
    val member: Member,
    val guild: Guild,
    val message: Message,
    val selfMember: Member = guild.selfMember,
    val guildIdLong: Long = guild.idLong
): BaseEvent {
    suspend fun reply(function: suspend CommandReply.() -> Unit) = function(CommandReply(this))

    fun replyAsync(function: CommandReply.() -> Unit) = function(CommandReply(this))

    fun isDeveloper(): Boolean = monke.handlers.get(ConfigHandler::class).config.developers.contains(user.id)
}