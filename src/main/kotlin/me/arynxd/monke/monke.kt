package me.arynxd.monke

import me.arynxd.monke.events.JDAEvents
import me.arynxd.monke.handlers.*
import me.arynxd.monke.objects.handlers.Handlers
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.objects.plugins.Plugins
import me.arynxd.monke.util.parseUptime
import me.arynxd.monke.util.plurify
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.restaction.MessageAction
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.lang.management.ManagementFactory
import java.text.DecimalFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.security.auth.login.LoginException
import kotlin.random.Random
import kotlin.system.exitProcess

fun main() {
    Monke()
}

class Monke : ListenerAdapter() {
    val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(10)
    val handlers = Handlers(this)
    val plugins = Plugins(this)

    val jda = build()

    private fun build(): JDA {
        try {
            return JDABuilder
                .create(
                    handlers.get(ConfigHandler::class).config.token,
                    GatewayIntent.GUILD_MEMBERS,

                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS,
                    GatewayIntent.GUILD_VOICE_STATES
                )
                .disableCache(
                    CacheFlag.ACTIVITY,
                    CacheFlag.EMOTE,
                    CacheFlag.CLIENT_STATUS,
                    CacheFlag.ROLE_TAGS,
                    CacheFlag.MEMBER_OVERRIDES
                )
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                .setHttpClient(handlers.okHttpClient)
                .addEventListeners(
                    this,
                    JDAEvents(this)
                )
                .setActivity(Activity.playing("loading up!"))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .also { jda ->
                    handlers.handlers.values.forEach { jda.addEventListeners(it) }
                }.build()
        }
        catch (exception: LoginException) {
            LOGGER.error(translateInternal("internal_error.invalid_login"))
            exitProcess(1)

        }
        catch (exception: IllegalArgumentException) {
            LOGGER.error(translateInternal("internal_error.invalid_build"))
            exitProcess(1)
        }
    }

    override fun onReady(event: ReadyEvent) {
        LOGGER.info("Loading handlers")
        handlers.enableHandlers()
        initTasks()

        handlers.get(MetricsHandler::class).guildCount.set(getGuildCount().toDouble())
        handlers.get(MetricsHandler::class).userCount.set(getUserCount().toDouble())

        MessageAction.setDefaultMentionRepliedUser(false)
        MessageAction.setDefaultMentions(emptyList())

        LOGGER.info("Loading plugins")
        plugins.loadPlugins()

        LOGGER.info(
            """
            
              __  __             _        _           _   
             |  \/  |           | |      | |         | |  
             | \  / | ___  _ __ | | _____| |__   ___ | |_ 
             | |\/| |/ _ \| '_ \| |/ / _ \ '_ \ / _ \| __|
             | |  | | (_) | | | |   <  __/ |_) | (_) | |_ 
             |_|  |_|\___/|_| |_|_|\_\___|_.__/ \___/ \__|
                                                            
                        ${handlers.get(ConfigHandler::class).config.api.website}
        """.trimIndent()
        )
    }

    private fun initTasks() {
        val taskHandler = handlers.get(TaskHandler::class)

        taskHandler.addRepeatingTask(30, TimeUnit.SECONDS) {
            handlers.get(PaginationHandler::class).cleanup()
        }

        taskHandler.addRepeatingTask(2, TimeUnit.MINUTES) {
            switchStatus()
        }
    }

    private fun switchStatus() {
        val random = Random

        val status = listOf(
            Activity.watching("${getGuildCount()} server" + getGuildCount().plurify()),
            Activity.watching("${getUserCount()} user" + getUserCount().plurify()),
            Activity.listening("your commands"),
            Activity.playing("forknife!!!!"),
            Activity.competing("among monkes"),
            Activity.listening("monkes"),
            Activity.playing("with monkes"),
            Activity.streaming("monke life", "https://zoo.sandiegozoo.org/cams/ape-cam"),
            Activity.watching("monkes grow")
        )

        jda.presence.setPresence(OnlineStatus.ONLINE, status[random.nextInt(status.size)])
    }

    fun getUserCount(): Long {
        return jda.guildCache.sumBy { it.memberCount }.toLong()
    }

    fun getJDAVersion(): String {
        return JDAInfo.VERSION
    }

    fun getJavaVersion(): String {
        return System.getProperty("java.version")
    }

    fun getMaxMemory(): Long {
        return Runtime.getRuntime().maxMemory()
    }

    fun getFreeMemory(): Long {
        return Runtime.getRuntime().freeMemory()
    }

    fun getTotalMemory(): Long {
        return Runtime.getRuntime().totalMemory()
    }

    fun getThreadCount(): Int {
        return ManagementFactory.getThreadMXBean().threadCount
    }

    fun getGuildCount(): Long {
        return jda.guildCache.size()
    }

    fun getMemoryFormatted(): String {
        return (getTotalMemory() - getFreeMemory() shr 20).toString() + "MB / " + (getMaxMemory() shr 20) + "MB"
    }

    fun getCPUUsage(): String {
        return DecimalFormat("#.##").format(ManagementFactory.getOperatingSystemMXBean().systemLoadAverage)
    }

    fun getUptimeString(): String {
        val millis = ManagementFactory.getRuntimeMXBean().uptime
        return parseUptime(
            Duration.between(
                LocalDateTime.now().minus(millis, ChronoUnit.MILLIS),
                LocalDateTime.now()
            )
        )
    }
}
