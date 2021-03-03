package me.arynxd.monke.objects.database

import me.arynxd.monke.DEFAULT_BOT_PREFIX
import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.DatabaseHandler
import me.arynxd.monke.objects.translation.Language
import org.ktorm.dsl.*
import org.ktorm.schema.Column

class GuildSettings(
    val guildId: Long,
    val monke: Monke
) {
    var prefix: String = getSetting(GUILDS.PREFIX, DEFAULT_BOT_PREFIX)
        set(value) {
            field = value
            setSetting(GUILDS.PREFIX, value)
        }

    var language: Language = Language.getLanguageByCode(getSetting(GUILDS.LANGUAGE, Language.DEFAULT.code))
        set(value) {
            field = value
            setSetting(GUILDS.LANGUAGE, value.code)
        }

    private fun <T : Any> getSetting(field: Column<T>, default: T): T {
        val query = monke.handlers.get(DatabaseHandler::class.java).database
            .from(GUILDS)
            .select(field)
            .where { GUILDS.GUILD_ID eq guildId }
            .rowSet

        if (!query.next()) {
            return default
        }

        return query[field] ?: default
    }

    private fun <T : Any> setSetting(field: Column<T>, value: T) {
        monke.handlers.get(DatabaseHandler::class.java).database
            .update(GUILDS) {
                set(field, value)
                where { GUILDS.GUILD_ID eq guildId }
            }
    }
}