package me.arynxd.monke

import dev.minn.jda.ktx.injectKTX
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.arynxd.monke.events.guildEvents
import me.arynxd.monke.events.messageEvents
import me.arynxd.monke.handlers.*
import me.arynxd.monke.objects.handlers.Handlers
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.util.parseUptime
import me.arynxd.monke.util.plurifyLong
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.lang.management.ManagementFactory
import java.text.DecimalFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.security.auth.login.LoginException
import kotlin.random.Random
import kotlin.system.exitProcess

fun main() {
    Monke()
}

class Monke: ListenerAdapter() {
    val handlers = Handlers(this)

    init {
        handlers.enableHandlers()
    }

    val jda: JDA = build()

    private fun build(): JDA {
        try {
            return JDABuilder
                .create(handlers.get(ConfigHandler::class).config.token,
                    GatewayIntent.GUILD_MEMBERS,

                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS,
                    GatewayIntent.GUILD_VOICE_STATES)

                .disableCache(
                    CacheFlag.ACTIVITY,
                    CacheFlag.EMOTE,
                    CacheFlag.CLIENT_STATUS,
                    CacheFlag.ROLE_TAGS,
                    CacheFlag.MEMBER_OVERRIDES)

                .injectKTX()
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                .setHttpClient(handlers.okHttpClient)
                .addEventListeners(this)
                .setActivity(Activity.playing("loading up!"))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .also { jda ->
                    handlers.handlers.values.forEach { jda.addEventListeners(it) }
                }.build()


        } catch (exception: LoginException) {
            LOGGER.error(TranslationHandler.getInternalString("internal_error.invalid_login"))
            exitProcess(1)

        } catch (exception: IllegalArgumentException) {
            LOGGER.error(TranslationHandler.getInternalString("internal_error.invalid_build"))
            exitProcess(1)
        }
    }

    override fun onReady(event: ReadyEvent) {
        initGuilds()
        initListeners()
        initTasks()

        LOGGER.info("""
            
              __  __             _        _           _   
             |  \/  |           | |      | |         | |  
             | \  / | ___  _ __ | | _____| |__   ___ | |_ 
             | |\/| |/ _ \| '_ \| |/ / _ \ '_ \ / _ \| __|
             | |  | | (_) | | | |   <  __/ |_) | (_) | |_ 
             |_|  |_|\___/|_| |_|_|\_\___|_.__/ \___/ \__|
                                                            
                        ${handlers.get(ConfigHandler::class).config.api.website}
        """.trimIndent())

        handlers.get(MetricsHandler::class).guildCount.set(getGuildCount().toDouble())
        handlers.get(MetricsHandler::class).userCount.set(getUserCount().toDouble())
    }

    private fun initGuilds() {
        jda.guildCache.forEach { handlers.get(GuildSettingsHandler::class).initGuild(it.idLong) }
    }

    private fun initListeners() {
        messageEvents()
        guildEvents()
    }

    private fun initTasks() {
        val jobHandler = handlers.get(JobHandler::class)

        jobHandler.addJob({
            while (true) {
                handlers.get(PaginationHandler::class).cleanup()
                delay(15_000)
            }
        })

        jobHandler.addJob({
            while (true) {
                switchStatus()
                delay(180_000) //2 Minutes
            }
        })
    }

    private fun switchStatus() {
        val random = Random

        val status = listOf(
            Activity.watching("${getGuildCount()} server" + plurifyLong(getGuildCount())),
            Activity.watching("${getUserCount()} user" + plurifyLong(getUserCount())),
            Activity.listening("your commands"),
            Activity.playing("forknife!!!!"),
            Activity.competing("among monkes"),
            Activity.listening("to monkes"),
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

    fun getMemoryPercent(): String {
        return ((getTotalMemory() - getFreeMemory()).toInt() / getMaxMemory() * 100).toString()
    }

    fun getCPUUsage(): String {
        return DecimalFormat("#.##").format(ManagementFactory.getOperatingSystemMXBean().systemLoadAverage)
    }

    fun getUptimeString(): String {
        val millis = ManagementFactory.getRuntimeMXBean().uptime
        return parseUptime(
            Duration.between(LocalDateTime.now().minus(millis, ChronoUnit.MILLIS),
            LocalDateTime.now())
        )
    }
}