package me.arynxd.monke.util

import com.google.errorprone.annotations.CheckReturnValue
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.requests.RestAction
import java.util.*

val MIN_PERMISSIONS: EnumSet<Permission> = EnumSet.of(
    Permission.MESSAGE_EMBED_LINKS,
    Permission.MESSAGE_HISTORY,
    Permission.MESSAGE_READ
)

@CheckReturnValue
fun Message.addReactions(vararg reactions: String): List<RestAction<Void>> {
    return reactions.map { this.addReaction(it) }
}

fun <T> Collection<RestAction<T>>.queue(
    success: ((T) -> Unit)? = null,
    failure: ((Throwable) -> Unit)? = null
) {
    this.forEach { it.queue(success, failure) }
}

fun TextChannel.hasMinimumPermissions(): Boolean {
    val selfMember = this.guild.selfMember
    return selfMember.hasPermission(this, MIN_PERMISSIONS)
}