package me.arynxd.monke.objects.events.types

import me.arynxd.monke.Monke

class CommandExceptionEvent(
    override val monke: Monke,
    val exception: Throwable
) : Event