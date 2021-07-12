package me.arynxd.monke.objects.handlers

import kotlin.reflect.KProperty

/**
 * Priority is ordered where 0 is loaded first
 */
class whenEnabled<T>(val priority: Int, val supplier: () -> T) {
    private var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw NoSuchElementException("Cannot get property before it has been set")
    }

    fun load() {
        value = supplier()
    }
}