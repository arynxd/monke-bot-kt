package me.arynxd.monke.util

import com.github.benmanes.caffeine.cache.LoadingCache
import javax.script.ScriptEngine

operator fun <K : Any, V : Any> LoadingCache<K, V>.set(key: K, value: V) {
    this.put(key, value)
}

operator fun ScriptEngine.set(key: String, value: Any) = this.put(key, value)