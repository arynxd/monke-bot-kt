package me.arynxd.monke.objects.events.interfaces

import me.arynxd.monke.objects.events.types.BaseEvent

interface IEventProcessor {
    fun registerListeners(vararg listeners: IEventListener)
    fun deregisterListeners(vararg listeners: IEventListener)
    fun fireEvent(event: BaseEvent)
}