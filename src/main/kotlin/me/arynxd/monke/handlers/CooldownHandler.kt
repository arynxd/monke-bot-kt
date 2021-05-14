package me.arynxd.monke.handlers

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.handlers.Handler
import net.dv8tion.jda.api.entities.User
import java.util.concurrent.TimeUnit

class CooldownHandler(
    override val monke: Monke,
) : Handler() {

    private val users: LoadingCache<Long, CooledUser> =
        Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build { CooledUser() }

    fun isOnCooldown(user: User, command: Command): Boolean {
        return users[user.idLong]!!.isOnCooldown(command)
    }

    fun getRemaining(user: User, command: Command): Long {
        return users[user.idLong]!!.getRemaining(command) - System.currentTimeMillis()
    }

    fun addCommand(user: User, command: Command) {
        users[user.idLong]!!.addCommand(command)
    }
}

class CooledUser {
    private val commands: MutableMap<Command, Long> = mutableMapOf()

    fun addCommand(command: Command) {
        commands[command] = System.currentTimeMillis() + command.metaData.cooldown
    }

    fun getRemaining(command: Command): Long {
        return commands[command] ?: 0L
    }

    fun isOnCooldown(command: Command): Boolean {
        val cooldown = commands[command] ?: return false
        return System.currentTimeMillis() <= cooldown
    }
}