package me.arynxd.monke

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.arynxd.monke.events.JDAEvents
import me.arynxd.monke.handlers.ConfigHandler
import me.arynxd.monke.handlers.MetricsHandler
import me.arynxd.monke.handlers.PaginationHandler
import me.arynxd.monke.handlers.TaskHandler
import me.arynxd.monke.handlers.translation.translateInternal
import me.arynxd.monke.objects.handlers.Handlers
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.objects.plugins.Plugins
import me.arynxd.monke.util.Debuggable
import me.arynxd.monke.util.classes.EmojiValidator
import me.arynxd.monke.util.classes.MonkeInfo
import me.arynxd.monke.util.plurify
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.restaction.MessageAction
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.security.auth.login.LoginException
import kotlin.random.Random
import kotlin.system.exitProcess

fun main() {
    Monke()
}

class Monke : ListenerAdapter(), Debuggable {
    val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(10)
    val coroutineScope = CoroutineScope(Dispatchers.Unconfined)
    val handlers = Handlers(this)
    val plugins = Plugins(this)

    val jda = build()

    private fun build(): JDA {
        try {
            return JDABuilder
                .create(
                    handlers[ConfigHandler::class].config.token,
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
                    CacheFlag.MEMBER_OVERRIDES,
                    CacheFlag.ONLINE_STATUS
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
            LOGGER.error(translateInternal { path = "internal_error.invalid_login" })
            exitProcess(1)

        }
        catch (exception: IllegalArgumentException) {
            LOGGER.error(translateInternal { path = "internal_error.invalid_build" })
            exitProcess(1)
        }
    }

    override fun onReady(event: ReadyEvent) {
        LOGGER.info("Loading handlers")
        handlers.enableHandlers()
        initTasks()

        handlers[MetricsHandler::class].guildCount.set(MonkeInfo.getGuildCount(jda).toDouble())
        handlers[MetricsHandler::class].userCount.set(MonkeInfo.getUserCount(jda).toDouble())

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
                                                            
                        ${handlers[ConfigHandler::class].config.api.website}
        """.trimIndent()
        )

        EmojiValidator.init()
    }

    private fun initTasks() {
        val taskHandler = handlers[TaskHandler::class]

        taskHandler.addRepeatingTask(30, TimeUnit.SECONDS) {
            handlers[PaginationHandler::class].cleanup()
        }

        taskHandler.addRepeatingTask(2, TimeUnit.MINUTES) {
            switchStatus()
        }
    }

    private fun switchStatus() {
        val random = Random

        val status = listOf(
            Activity.watching("${MonkeInfo.getGuildCount(jda)} server" + MonkeInfo.getGuildCount(jda).plurify()),
            Activity.watching("${MonkeInfo.getUserCount(jda)} user" + MonkeInfo.getUserCount(jda).plurify()),
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

    override fun toDebugString(): String {
        return """
            Handlers:       ${this.handlers.size()}
            Uptime:         ${MonkeInfo.uptimeString}
            CPU:            ${MonkeInfo.cpuUsageFormatted}
            Threads:        ${MonkeInfo.threadCount}
            EmojiValidator: ${EmojiValidator.state}
        """.trimIndent()
    }
}
