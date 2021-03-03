package me.arynxd.monke.objects.ratelimit

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class RateLimiter {
    private val buckets = ConcurrentHashMap<RateLimitedAction, RateLimitBucket>()

    fun take(action: RateLimitedAction) {
        val bucket = buckets[action]

        if (bucket == null) {
            buckets[action] = RateLimitBucket(action.maxAmount, action.refresh, action.unit)
        }

        buckets[action]!!.take()
    }

    fun canTake(action: RateLimitedAction): Boolean {
        val bucket = buckets[action]

        if (bucket == null) {
            buckets[action] = RateLimitBucket(action.maxAmount, action.refresh, action.unit)
            return true
        }

        return bucket.canTake()
    }

    class RateLimitBucket(
        private val maxUsage: Long,
        private val refresh: Long,
        private val unit: TimeUnit
    ) {
        private var usage: Long = 0
        private var lastUsed = System.currentTimeMillis()

        fun canTake(): Boolean {
            if ((lastUsed + unit.toMillis(refresh)) < System.currentTimeMillis()) {
                usage = 0
                return true
            }
            val use = usage + 1
            return use <= maxUsage
        }

        fun take() {
            usage ++
            lastUsed = System.currentTimeMillis()
        }
    }
}