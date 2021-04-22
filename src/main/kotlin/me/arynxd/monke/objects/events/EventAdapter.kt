package me.arynxd.monke.objects.events

import me.arynxd.monke.objects.events.interfaces.IEventListener
import me.arynxd.monke.objects.events.types.command.CommandEvent
import me.arynxd.monke.objects.events.types.command.CommandExceptionEvent
import me.arynxd.monke.objects.events.types.command.CommandPreprocessEvent
import me.arynxd.monke.objects.events.types.BaseEvent
import me.arynxd.monke.objects.handlers.LOGGER

open class EventAdapter : IEventListener {
    // Command events
    open fun onCommandEvent(event: CommandEvent) { }
    open fun onCommandPreprocessEvent(event: CommandPreprocessEvent) { }
    open fun onCommandExceptionEvent(event: CommandExceptionEvent) { }

    override fun onEvent(event: BaseEvent) {
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