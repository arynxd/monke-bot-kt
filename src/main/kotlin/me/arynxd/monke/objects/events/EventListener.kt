package me.arynxd.monke.objects.events

import me.arynxd.monke.objects.events.types.Event

interface EventListener {
    fun onEvent(event: Event)
}