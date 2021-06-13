package me.arynxd.monke.util

import com.github.benmanes.caffeine.cache.LoadingCache

fun <T> List<T>.subList(start: Int) = this.subList(start, this.size)

operator fun <K : Any, V : Any> LoadingCache<K, V>.set(key: K, value: V) {
    this.put(key, value)
}