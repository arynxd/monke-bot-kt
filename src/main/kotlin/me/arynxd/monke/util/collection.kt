package me.arynxd.monke.util

fun <T> HashSet<T>.get(index: Int): T {
    if (index < 0 || index > this.size) {
        throw IndexOutOfBoundsException()
    }

    for ((i, el) in this.withIndex()) {
        if (i == index) {
            return el
        }
    }

    throw NoSuchElementException()
}