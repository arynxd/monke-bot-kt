package me.arynxd.monke.handlers

import dev.minn.jda.ktx.await
import io.prometheus.client.Counter
import io.prometheus.client.Gauge
import io.prometheus.client.exporter.HTTPServer
import io.prometheus.client.hotspot.DefaultExports
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.exception.HandlerException
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.handlers.LOGGER
import net.dv8tion.jda.api.events.DisconnectEvent
import net.dv8tion.jda.api.events.ResumedEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.http.HttpRequestEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.io.IOException
import javax.annotation.Nonnull


//ty topi 👀 https://github.com/KittyBot-Org/KittyBot/blob/master/src/main/java/de/kittybot/kittybot/modules/PrometheusModule.java
class MetricsHandler @JvmOverloads constructor(
    override val monke: Monke,
    override val dependencies: List<Class<out Handler>> = listOf(ConfigHandler::class.java)
) : Handler() {

    private val port: Int by lazy { getPrometheusPort() }

    private val musicEvents: Counter = Counter.build()
        .name("monkebot_track_event")
        .help("Music Track Events (failed/loaded/searched)")
        .labelNames("type")
        .register()

    val commandCounter: Counter = Counter.build()
        .name("monkebot_commands")
        .help("Amounts of commands ran by name.")
        .labelNames("name")
        .register()

    val guildCount: Gauge = Gauge.build()
        .name("monkebot_guilds")
        .help("Guild Count")
        .register()

    val userCount: Gauge = Gauge.build()
        .name("monkebot_users")
        .help("User Count")
        .register()

    private val httpRequests = Counter.build()
        .name("monkebot_http_requests")
        .help("Successful HTTP Requests (JDA)")
        .register()

    private val http429Requests = Counter.build()
        .name("monkebot_http_ratelimit_requests")
        .help("429 HTTP Requests (JDA)")
        .register()

    private val messageCount: Counter = Counter.build()
        .name("monkebot_messages_received")
        .help("Received messages (all users + bots)")
        .register()

    private val botEvents: Counter = Counter.build()
        .name("monkebot_events")
        .help("Bot Events")
        .labelNames("type")
        .register()

    private val restPing: Gauge = Gauge.build()
        .name("monkebot_rest_ping")
        .help("REST ping in ms")
        .register()

    private val gatewayPing: Gauge = Gauge.build()
        .name("monkebot_gateway_ping")
        .help("Gateway ping in ms")
        .register()


    override fun onEnable() {
        if (port == -1) {
            LOGGER.warn("MetricsHandler offline.")
            return
        }
        DefaultExports.initialize()

        try {
            HTTPServer(port)
        } catch (exception: IOException) {
            LOGGER.error("MetricsHandler offline.", exception)
        }

        GlobalScope.launch {
            while (true) {
                restPing.set(monke.jda.restPing.await().toDouble())
                gatewayPing.set(monke.jda.gatewayPing.toDouble())
                delay(150_000) //2.5 Minutes
            }
        }
    }

    override fun onResumed(event: ResumedEvent) {
        botEvents.labels("resumed").inc()
    }

    override fun onDisconnect(event: DisconnectEvent) {
        botEvents.labels("disconnect").inc()
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        messageCount.inc()
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        guildCount.set(monke.getGuildCount().toDouble())
        userCount.set(monke.getUserCount().toDouble())
    }

    override fun onGuildLeave(@Nonnull event: GuildLeaveEvent) {
        guildCount.set(monke.getGuildCount().toDouble())
        userCount.set(monke.getUserCount().toDouble())
    }

    override fun onHttpRequest(event: HttpRequestEvent) {
        if (event.isRateLimit) {
            http429Requests.inc()
        }
        httpRequests.inc()
    }

    private fun getPrometheusPort(): Int {
        return monke.handlers.get(ConfigHandler::class.java)
            .config
            .prometheus
            .port.toIntOrNull() ?: throw HandlerException("Prometheus port was not a number.")
    }
}