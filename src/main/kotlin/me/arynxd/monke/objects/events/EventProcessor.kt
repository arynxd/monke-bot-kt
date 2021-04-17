package me.arynxd.monke.objects.events

import me.arynxd.monke.objects.events.interfaces.IEventListener
import me.arynxd.monke.objects.events.interfaces.IEventProcessor
import me.arynxd.monke.objects.events.types.BaseEvent

class EventProcessor: IEventProcessor {
    private val listeners = mutableListOf<IEventListener>()

    override fun registerListeners(vararg listeners: IEventListener) {
        this.listeners.addAll(listeners)
    }

    override fun deregisterListeners(vararg listeners: IEventListener) {
        this.listeners.removeAll(listeners)
    }

    override fun fireEvent(event: BaseEvent) {
        for (listener in listeners) {
            listener.onEvent(event)
        }
    }
}