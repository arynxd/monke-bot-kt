package me.arynxd.monke.objects.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

class MessageCache(val guildData: GuildData) {
    private val messages: Cache<Long, CachedMessage> =
        Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build()

    fun addMessage(message: CachedMessage) {
        messages.put(message.id, message)
    }

    fun removeMessage(message: CachedMessage) {
        messages.invalidate(message.id)
    }

    fun getMessage(id: Long): CachedMessage? {
        return messages.getIfPresent(id)
    }
}

data class CachedMessage(
    val id: Long,
    val content: String,
    val timeStamp: OffsetDateTime,
    val author: CachedAuthor
)

data class CachedAuthor(
    val id: Long
) {
    fun getAsMention() = "<@!${id}>"
}