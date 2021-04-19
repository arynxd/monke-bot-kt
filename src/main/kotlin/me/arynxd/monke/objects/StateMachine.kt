package me.arynxd.monke.objects

import net.dv8tion.jda.api.events.GenericEvent
import kotlin.reflect.KClass
import kotlin.reflect.cast

class StateMachine <T : GenericEvent> (
    val clazz: KClass<T>, val predicate: (T) -> Boolean, val transformer: (T, Int, StateMachine<*>) -> Int
) {
    private var state = 0

    fun onEvent(event: GenericEvent) {

        if (clazz.isInstance(event)) {
            val castedEvent = clazz.cast(event)

            if (!predicate(castedEvent)) {
                return
            }
            state = transformer(castedEvent, state, this)
        }
    }
}