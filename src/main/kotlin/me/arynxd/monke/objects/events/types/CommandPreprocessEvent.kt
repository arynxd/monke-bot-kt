package me.arynxd.monke.objects.events.types

import me.arynxd.monke.Monke
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent

class CommandPreprocessEvent(
    override val monke: Monke,
    val message: Message,
    val jda: JDA,
    val channel: TextChannel,
    val user: User,
    val member: Member,
    val guild: Guild
) : Event {
    constructor(event: GuildMessageReceivedEvent, monke: Monke) : this(
        monke,
        event.message,
        event.jda,
        event.channel,
        event.author,
        event.member ?: throw IllegalStateException("Member was null"),
        event.guild
    )

    constructor(event: GuildMessageUpdateEvent, monke: Monke) : this(
        monke,
        event.message,
        event.jda,
        event.channel,
        event.author,
        event.member ?: throw IllegalStateException("Member was null"),
        event.guild
    )
}
