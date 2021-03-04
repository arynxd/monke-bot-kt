package me.arynxd.monke.handlers

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.database.GUILDS
import me.arynxd.monke.objects.database.GuildSettings
import me.arynxd.monke.objects.handlers.Handler
import org.ktorm.dsl.insert
import org.ktorm.support.postgresql.insertOrUpdate
import java.util.concurrent.TimeUnit

class GuildSettingsHandler @JvmOverloads constructor(
    override val monke: Monke,
    override val dependencies: List<Class<out Handler>> = listOf(DatabaseHandler::class.java)
) : Handler() {

    private val settingsCache: LoadingCache<Long, GuildSettings> =
        Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build() { GuildSettings(it, monke) }

    fun getCache(guildId: Long): GuildSettings {
        return settingsCache.get(guildId)!!
    }

    fun initGuild(guildId: Long) {
        monke.handlers.get(DatabaseHandler::class.java).database
            .insertOrUpdate(GUILDS) {
                set(GUILDS.GUILD_ID, guildId)
                onConflict() { set(GUILDS.GUILD_ID, guildId) } //Do nothing
            }
    }
}