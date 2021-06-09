package me.arynxd.monke.objects.handlers

import kotlin.reflect.KProperty

class whenEnabled<T>(val supplier: () -> T) {
    private var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw NoSuchElementException("Cannot get property before it has been set")
    }

    fun load() {
        value = supplier()
    }
}