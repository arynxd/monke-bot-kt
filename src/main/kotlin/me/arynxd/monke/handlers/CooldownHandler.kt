package me.arynxd.monke.handlers

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.cache.CooldownCache
import me.arynxd.monke.objects.cache.CooledUser
import me.arynxd.monke.objects.handlers.Handler
import java.util.concurrent.TimeUnit

class CooldownHandler(
    override val monke: Monke,
) : Handler() {
    private val caches: LoadingCache<Long, CooldownCache> =
        Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build { CooldownCache() }

    fun getCache(guildId: Long) = caches[guildId]!!
}

