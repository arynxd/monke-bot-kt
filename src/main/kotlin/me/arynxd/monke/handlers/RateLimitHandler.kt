package me.arynxd.monke.handlers

import me.arynxd.monke.Monke
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.ratelimit.RateLimiter
import java.util.concurrent.ConcurrentHashMap

class RateLimitHandler(
    override val monke: Monke,
) : Handler() {

    private val limiters = ConcurrentHashMap<Long, RateLimiter>()

    fun getRateLimiter(guildId: Long): RateLimiter {
        val limiter = limiters[guildId]

        if (limiter == null) {
            limiters[guildId] = RateLimiter()
        }

        return limiters[guildId]!!
    }
}