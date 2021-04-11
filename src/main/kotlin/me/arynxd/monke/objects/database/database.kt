package me.arynxd.monke.objects.database

import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

object GUILDS : Table<Nothing>("guilds") {
    val GUILD_ID = long("guild_id").primaryKey()
    val LOG_CHANNEL = long("log_channel")
    val PREFIX = varchar("prefix")
    val LANGUAGE = varchar("language")
}