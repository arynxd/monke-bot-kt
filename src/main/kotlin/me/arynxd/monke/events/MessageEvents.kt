package me.arynxd.monke.events

import dev.minn.jda.ktx.listener
import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.CommandHandler
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent
import java.lang.IllegalStateException

fun Monke.messageEvents() {
    jda.listener<GuildMessageReceivedEvent> {
        if (it.author.isBot || it.isWebhookMessage) {
            return@listener
        }
        handlers.get(CommandHandler::class.java).handle(GuildMessageEvent(it))

    }

    jda.listener<GuildMessageUpdateEvent> {
        if (it.author.isBot) {
            return@listener
        }

        handlers.get(CommandHandler::class.java).handle(GuildMessageEvent(it))
    }
}

class GuildMessageEvent() {
    val message: Message
    val jda: JDA
    val messageChannel: MessageChannel
    val channel: TextChannel
    val user: User
    val member: Member
    val guild: Guild

    constructor(event: GuildMessageReceivedEvent): this() {
        this.message = event.message
        this.jda = event.jda
        this.messageChannel = event.channel
        this.channel = event.channel
        this.user = event.author
        this.member = event.member?: throw IllegalStateException("Member was null")
        this.guild = event.guild
    }

    constructor(event: GuildMessageUpdateEvent): this() {
        this.message = event.message
        this.jda = event.jda
        this.messageChannel = event.channel
        this.channel = event.channel
        this.user = event.author
        this.member = event.member?: throw IllegalStateException("Member was null")
        this.guild = event.guild
    }
}
