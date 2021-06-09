package me.arynxd.monke.util

import com.google.errorprone.annotations.CheckReturnValue
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.requests.RestAction

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