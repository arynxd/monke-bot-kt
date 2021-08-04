package me.arynxd.monke.objects.events.interfaces

import me.arynxd.monke.objects.events.types.BaseEvent

interface IEventListener {
    fun onEvent(event: BaseEvent)
}