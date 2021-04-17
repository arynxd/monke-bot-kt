package me.arynxd.monke.objects.events

import me.arynxd.monke.Monke
import me.arynxd.monke.objects.events.types.Event

class EventProcessor(val monke: Monke) {
    private val listeners = mutableListOf<EventListener>()

    fun registerListeners(vararg listener: EventListener) {
        listeners.addAll(listener)
    }

    fun deregisterListeners(vararg listener: EventListener) {
        listeners.removeAll(listener)
    }

    fun fireEvent(event: Event) {
        for (listener in listeners) {
            listener.onEvent(event)
        }
    }
}