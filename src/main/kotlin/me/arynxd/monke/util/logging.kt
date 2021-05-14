package me.arynxd.monke.util

import org.slf4j.Logger

fun Logger.debug(obj: Debuggable) {
    if (this.isDebugEnabled) {
        this.debug("${obj::class.simpleName}@${obj.hashCode()} - ${obj.toDebugString()}")
    }
}

interface Debuggable {
    fun toDebugString(): String
}