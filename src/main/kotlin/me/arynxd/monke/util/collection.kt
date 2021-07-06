package me.arynxd.monke.util

import com.github.benmanes.caffeine.cache.LoadingCache
import java.lang.StringBuilder

fun <T> List<T>.subList(start: Int) = this.subList(start, this.size)

fun <K : Any, V : Any> LoadingCache<K, V>.toPrettyString(): String {
    val map = this.asMap()
    val builder = StringBuilder().append("{")
    map.forEach { (k, v) -> builder.append(k).append(" -> ").append(v).append(" ") }
    builder.append("}")
    return builder.toString()
}

