package me.arynxd.monke.handlers

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.arynxd.monke.launch.Monke
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.handlers.LOGGER
import java.io.File
import kotlin.system.exitProcess

class ConfigHandler(
    override val monke: Monke
) : Handler() {
    private val formatter = Json { prettyPrint = true; isLenient = true }

    val configPath = monke.config.configPath

    private val configFile = File(configPath)

    init {
        initFile()
    }

    val config = loadFile()

    private fun initFile() {
        if (!configFile.exists()) {
            configFile.createNewFile()

            val defaults = formatter.encodeToString(
                ConfigFile(
                    token = "token",
                    developers = listOf("1".repeat(10)),
                    logChannel = "channel-id",
                    logWebhook = "webhook-url",
                    preferredLanguage = "en_US",

                    database = DatabaseConfiguration(
                        username = "username",
                        password = "password",
                        jdbcURL = "jdbc:postgresql://host:port/database",
                        driverName = "org.postgresql.Driver"
                    ),

                    api = APIConfiguration(
                        website = "website",
                        discordInvite = "support-server-invite",
                        botInvite = "bot-invite",
                    ),

                    prometheus = PrometheusConfiguration(
                        "-1"
                    )
                )
            )

            configFile.writeText(defaults)
        }
    }

    private fun loadFile(): ConfigFile {
        try {
            return Json.decodeFromString(configFile.readLines().joinToString(separator = "\n"))
        }
        catch (exception: Exception) {
            // This cannot be translated since.. the language comes from the config file
            LOGGER.error("Something went wrong with the JSON file, please ensure it is correct.", exception)
            exitProcess(1)
        }
    }

    @Serializable
    data class ConfigFile(
        val token: String,
        val developers: List<String>,
        val logChannel: String,
        val logWebhook: String,
        val preferredLanguage: String,
        val database: DatabaseConfiguration,
        val api: APIConfiguration,
        val prometheus: PrometheusConfiguration
    )

    @Serializable
    data class DatabaseConfiguration(
        val username: String,
        val password: String,
        val jdbcURL: String,
        val driverName: String
    )

    @Serializable
    data class APIConfiguration(
        val website: String,
        val discordInvite: String,
        val botInvite: String,
    )

    @Serializable
    data class PrometheusConfiguration(
        val port: String
    )
}