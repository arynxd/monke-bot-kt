package me.arynxd.monke.objects.events

import me.arynxd.monke.objects.events.types.CommandEvent
import me.arynxd.monke.objects.events.types.CommandExceptionEvent
import me.arynxd.monke.objects.events.types.CommandPreprocessEvent
import me.arynxd.monke.objects.events.types.Event
import me.arynxd.monke.objects.handlers.LOGGER

open class EventAdapter : EventListener {
    // Command events
    open fun onCommandEvent(event: CommandEvent) { }
    open fun onCommandPreprocessEvent(event: CommandPreprocessEvent) { }
    open fun onCommandExceptionEvent(event: CommandExceptionEvent) { }

    override fun onEvent(event: Event) {
        try {
            when (event) {
                is CommandPreprocessEvent -> onCommandPreprocessEvent(event)
                is CommandEvent -> onCommandEvent(event)
                is CommandExceptionEvent -> onCommandExceptionEvent(event)
            }
        }
        catch (exception: Exception) {
            LOGGER.error("An event listener had an uncaught exception", exception)
        }
    }
}