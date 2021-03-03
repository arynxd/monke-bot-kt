package me.arynxd.monke.handlers

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.handlers.Handler
import net.dv8tion.jda.api.entities.User
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

class CooldownHandler @JvmOverloads constructor(
    override val monke: Monke,
    override val dependencies: List<Class<out Handler>> = listOf()
) : Handler {

    private val users: LoadingCache<Long, CooledUser> =
        Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build() { CooledUser() }

    fun isOnCooldown(user: User, command: Command): Boolean {
        return users[user.idLong]!!.isOnCooldown(command)
    }

    fun getRemaining(user: User, command: Command): Long {
        return users[user.idLong]!!.getRemaining(command) - System.currentTimeMillis()
    }

    fun addCommand(user: User, command: Command) {
        users[user.idLong]!!.addCommand(command)
    }

    override fun onEnable() {
        //Unused
    }

    override fun onDisable() {
        //Unused
    }

    class CooledUser {
        private var commands: MutableMap<Command, Long> = mutableMapOf()

        fun addCommand(command: Command) {
            commands[command] = System.currentTimeMillis() + command.cooldown
        }

        fun getRemaining(command: Command): Long {
            return commands[command]?: 0L
        }

        fun isOnCooldown(command: Command): Boolean {
            val cooldown = commands[command] ?: return false
            return System.currentTimeMillis() < cooldown
        }
    }
}