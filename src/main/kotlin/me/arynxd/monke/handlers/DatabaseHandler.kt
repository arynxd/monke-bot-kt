package me.arynxd.monke.handlers

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.util.convertToString
import me.arynxd.monke.util.loadResource
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect
import kotlin.system.exitProcess

class DatabaseHandler @JvmOverloads constructor(
    override val monke: Monke,
    override val dependencies: List<Class<out Handler>> = listOf(
        ConfigHandler::class.java,
        TranslationHandler::class.java
    )
) : Handler {
    private lateinit var pool: HikariDataSource
    lateinit var database: Database

    override fun onEnable() {
        val hikariConfig = HikariConfig()
        val configuration = monke.handlers.get(ConfigHandler::class.java).config.database

        hikariConfig.driverClassName = configuration.driverName
        hikariConfig.jdbcUrl = configuration.jdbcURL

        hikariConfig.username = configuration.username
        hikariConfig.password = configuration.password

        hikariConfig.maximumPoolSize = 30
        hikariConfig.minimumIdle = 10
        hikariConfig.connectionTimeout = 10000

        hikariConfig.poolName = "DatabasePool"

        try {
            pool = HikariDataSource(hikariConfig)
            database = Database.connect(
                dataSource = pool,
                dialect = PostgreSqlDialect()
            )
            initTables()
        } catch (exception: Exception) {
            LOGGER.error(
                TranslationHandler.getString(Language.EN_US, "internal_error.database_offline"), exception
            )
            exitProcess(1)
        }
    }

    override fun onDisable() {
        pool.close()
    }

    private fun initTables() {
        initTable("guilds")
    }

    private fun initTable(table: String) {
        val sql = convertToString(loadResource("sql/$table.sql"))
        pool.connection.createStatement().execute(sql)
    }
}