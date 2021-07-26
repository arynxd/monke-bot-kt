package me.arynxd.monke.handlers

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import me.arynxd.monke.launch.Monke
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.ratelimit.RateLimiter

class RateLimitHandler(
    override val monke: Monke,
) : Handler() {
    private val limiters: LoadingCache<Long, RateLimiter> =
        Caffeine.newBuilder()
            .build { RateLimiter() }

    fun getRateLimiter(guildId: Long): RateLimiter {
        return limiters[guildId]!!
    }
}