package me.arynxd.monke.handlers

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.cache.CachedAuthor
import me.arynxd.monke.objects.cache.CachedMessage
import me.arynxd.monke.objects.cache.GuildData
import me.arynxd.monke.objects.database.GUILDS
import me.arynxd.monke.objects.handlers.Handler
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.ktorm.support.postgresql.insertOrUpdate
import kotlin.reflect.KClass

class GuildDataHandler @JvmOverloads constructor(
    override val monke: Monke,
    override val dependencies: List<KClass<out Handler>> = listOf(DatabaseHandler::class)
) : Handler() {

    private val dataCache: LoadingCache<Long, GuildData> =
        Caffeine.newBuilder()
            .build { GuildData(it, monke) }

    fun getCache(guildId: Long): GuildData {
        return dataCache.get(guildId)!!
    }

    fun initGuild(guildId: Long) {
        monke.handlers.get(DatabaseHandler::class).database
            .insertOrUpdate(GUILDS) {
                set(GUILDS.GUILD_ID, guildId)
                onConflict { set(GUILDS.GUILD_ID, guildId) } //Do nothing
            }
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        val cache = dataCache.get(event.guild.idLong) ?: throw IllegalStateException("GuildData was null")
        val message = event.message
        val author = event.author

        cache.messageCache.addMessage(
            CachedMessage(
                id = message.idLong,
                content = message.contentRaw,
                timeStamp = message.timeCreated,

                author = CachedAuthor(
                    id = author.idLong
                )
            )
        )
    }
}