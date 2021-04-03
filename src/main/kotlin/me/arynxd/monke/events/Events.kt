package me.arynxd.monke.events

import dev.minn.jda.ktx.Embed
import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.CommandHandler
import me.arynxd.monke.handlers.ConfigHandler
import me.arynxd.monke.handlers.GuildDataHandler
import me.arynxd.monke.util.DEFAULT_EMBED_COLOUR
import me.arynxd.monke.util.plurifyLong
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.time.Instant

class Events(val monke: Monke) : ListenerAdapter() {
    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.author.isBot) {
            return
        }

        monke.handlers.get(CommandHandler::class).handle(GuildMessageEvent(event))
    }

    override fun onGuildMessageUpdate(event: GuildMessageUpdateEvent) {
        if (event.author.isBot) {
            return
        }

        monke.handlers.get(CommandHandler::class).handle(GuildMessageEvent(event))
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        val jda = event.jda
        val channel = jda.getTextChannelById(monke.handlers.get(ConfigHandler::class).config.logChannel) ?: return
        val serverCount = jda.guildCache.size()

        event.guild.retrieveOwner().queue {
            channel.sendMessage(
                Embed(
                    title = "Joined a new server!",
                    description = "Server: *${it.guild.name}* with owner **${
                        it.user.asTag
                    }**\n\n" +
                            "Now at **${serverCount}** server${plurifyLong(serverCount)}.",
                    color = DEFAULT_EMBED_COLOUR.rgb,
                    timestamp = Instant.now()
                )
            ).queue()
        }

        monke.handlers.get(GuildDataHandler::class).initGuild(event.guild.idLong)
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        val jda = event.jda
        val channel = jda.getTextChannelById(monke.handlers.get(ConfigHandler::class).config.logChannel) ?: return
        val serverCount = jda.guildCache.size()

        event.guild.retrieveOwner().queue {
            channel.sendMessage(
                Embed(
                    title = "Left a server :(",
                    description = "Server: *${it.guild.name}* with owner **${
                        it.user.asTag
                    }**\n\n" +
                            "Now at **${serverCount}** server${plurifyLong(serverCount)}.",
                    color = DEFAULT_EMBED_COLOUR.rgb,
                    timestamp = Instant.now()
                )
            ).queue()
        }
    }
}

class GuildMessageEvent(
    val message: Message,
    val jda: JDA,
    val channel: TextChannel,
    val user: User,
    val member: Member,
    val guild: Guild
) {
    constructor(event: GuildMessageReceivedEvent) : this(
        event.message,
        event.jda,
        event.channel,
        event.author,
        event.member ?: throw IllegalStateException("Member was null"),
        event.guild
    )

    constructor(event: GuildMessageUpdateEvent) : this(
        event.message,
        event.jda,
        event.channel,
        event.author,
        event.member ?: throw IllegalStateException("Member was null"),
        event.guild
    )
}