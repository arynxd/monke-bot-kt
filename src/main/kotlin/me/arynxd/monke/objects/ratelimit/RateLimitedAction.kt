package me.arynxd.monke.objects.ratelimit

import java.util.concurrent.TimeUnit

enum class RateLimitedAction(
    val maxAmount: Long,
    val refresh: Long,
    val unit: TimeUnit
) {
    EMOJI_CREATE(
        maxAmount = 20,
        refresh = 1, unit =
        TimeUnit.DAYS
    ),

    BULK_DELETE(
        maxAmount = 1,
        refresh = 10,
        unit = TimeUnit.SECONDS
    )
}