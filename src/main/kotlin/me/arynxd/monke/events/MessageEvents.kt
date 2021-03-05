package me.arynxd.monke.events

import dev.minn.jda.ktx.listener
import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.CommandHandler
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent

fun Monke.messageEvents() {
    jda.listener<GuildMessageReceivedEvent> {
        if (it.author.isBot || it.isWebhookMessage) {
            return@listener
        }
        handlers.get(CommandHandler::class).handle(GuildMessageEvent(it))

    }

    jda.listener<GuildMessageUpdateEvent> {
        if (it.author.isBot) {
            return@listener
        }

        handlers.get(CommandHandler::class).handle(GuildMessageEvent(it))
    }
}

class GuildMessageEvent() {
    lateinit var message: Message
    lateinit var jda: JDA
    lateinit var messageChannel: MessageChannel
    lateinit var channel: TextChannel
    lateinit var user: User
    lateinit var member: Member
    lateinit var guild: Guild

    constructor(event: GuildMessageReceivedEvent) : this() {
        message = event.message
        jda = event.jda
        messageChannel = event.channel
        channel = event.channel
        user = event.author
        member = event.member ?: throw IllegalStateException("Member was null")
        guild = event.guild
    }

    constructor(event: GuildMessageUpdateEvent) : this() {
        message = event.message
        jda = event.jda
        messageChannel = event.channel
        channel = event.channel
        user = event.author
        member = event.member ?: throw IllegalStateException("Member was null")
        guild = event.guild
    }
}