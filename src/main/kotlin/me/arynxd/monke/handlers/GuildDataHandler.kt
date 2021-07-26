package me.arynxd.monke.handlers

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import me.arynxd.monke.launch.Monke
import me.arynxd.monke.objects.cache.GuildData
import me.arynxd.monke.objects.database.GUILDS
import me.arynxd.monke.objects.handlers.Handler
import org.ktorm.support.postgresql.insertOrUpdate
import kotlin.reflect.KClass

class GuildDataHandler(
    override val monke: Monke,
    override val dependencies: List<KClass<out Handler>> = listOf(DatabaseHandler::class)
) : Handler() {

    private val dataCache: LoadingCache<Long, GuildData> =
        Caffeine.newBuilder()
            .build { GuildData(it, monke) }

    fun getData(guildId: Long): GuildData {
        return dataCache.get(guildId)!!
    }

    fun initGuild(guildId: Long) {
        monke.handlers[DatabaseHandler::class].db
            .insertOrUpdate(GUILDS) {
                set(GUILDS.GUILD_ID, guildId)
                onConflict { set(GUILDS.GUILD_ID, guildId) } //Do nothing
            }
    }

    override fun onEnable() {
        monke.jda.guildCache.forEach {
            initGuild(it.idLong)
        }
    }
}