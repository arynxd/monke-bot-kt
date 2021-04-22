package me.arynxd.monke.objects.events.types.command

import me.arynxd.monke.Monke
import me.arynxd.monke.objects.events.types.BaseEvent

class CommandExceptionEvent(
    override val monke: Monke,
    val exception: Throwable,
    origin: CommandEvent
) : BaseEvent, GenericCommandEvent(
        monke = monke,

        jda = origin.jda,
        channel = origin.channel,
        user = origin.user,
        member = origin.member,
        guild = origin.guild,
        message = origin.message
    )