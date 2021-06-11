package me.arynxd.monke.handlers

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.objects.handlers.whenEnabled
import me.arynxd.monke.util.loadResource
import me.arynxd.monke.util.readFully
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect
import kotlin.reflect.KClass
import kotlin.system.exitProcess

class DatabaseHandler(
    override val monke: Monke,
    override val dependencies: List<KClass<out Handler>> = listOf(
        ConfigHandler::class,
        TranslationHandler::class
    )
) : Handler() {
    private val pool: HikariDataSource by whenEnabled { getHikari() }
    val db: Database by lazy { getKtorm() }

    private fun getHikari(): HikariDataSource {
        val hikariConfig = HikariConfig()
        val configuration = monke.handlers[ConfigHandler::class].config.database

        hikariConfig.driverClassName = configuration.driverName
        hikariConfig.jdbcUrl = configuration.jdbcURL

        hikariConfig.username = configuration.username
        hikariConfig.password = configuration.password

        hikariConfig.maximumPoolSize = 30
        hikariConfig.minimumIdle = 10
        hikariConfig.connectionTimeout = 10000

        hikariConfig.poolName = "DatabasePool"

        return try {
            HikariDataSource(hikariConfig)
        }
        catch (exception: IllegalArgumentException) {
            LOGGER.error(
                translateInternal {
                    path = "internal_error.database_offline"
                    values = arrayOf(exception)
                }
            )
            exitProcess(1)
        }
    }

    override fun onEnable() {
        initTables()
    }

    private fun getKtorm() = Database.connect(
        dataSource = pool,
        dialect = PostgreSqlDialect()
    )

    override fun onDisable() {
        pool.close()
    }

    private fun initTables() {
        initTable("guilds")
    }

    private fun initTable(table: String) {
        val sql = loadResource("sql/$table.sql").readFully()
        pool.connection.createStatement().execute(sql)
    }
}