package me.arynxd.monke.objects.events.types.command

import me.arynxd.monke.Monke
import me.arynxd.monke.objects.events.types.BaseEvent
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent

class CommandPreprocessEvent(
    override val monke: Monke,
    message: Message,
    jda: JDA,
    channel: TextChannel,
    user: User,
    member: Member,
    guild: Guild
) : BaseEvent, GenericCommandEvent(
    monke = monke,

    message = message,
    jda = jda,
    channel = channel,
    user = user,
    member = member,
    guild = guild
) {
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
