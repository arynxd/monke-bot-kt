package me.arynxd.monke.objects.events

import me.arynxd.monke.objects.events.interfaces.IEventListener
import me.arynxd.monke.objects.events.interfaces.IEventProcessor
import me.arynxd.monke.objects.events.types.BaseEvent
import me.arynxd.monke.objects.handlers.LOGGER
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

class EventProcessor: IEventProcessor {
    private val listeners = mutableListOf<IEventListener>()
    private val pool = Executors.newFixedThreadPool(10) { Thread(it, "Monke-Event-Thread") }

    override fun registerListeners(vararg listeners: IEventListener) {
        this.listeners.addAll(listeners)
    }

    override fun deregisterListeners(vararg listeners: IEventListener) {
        this.listeners.removeAll(listeners)
    }

    override fun fireEvent(event: BaseEvent) {
        for (listener in listeners) {
            pool.submit() {
                try {
                    listener.onEvent(event)
                }
                catch (exception: Exception) {
                    LOGGER.error("An event listener had an uncaught exception", exception)
                }
            }
        }
    }
}