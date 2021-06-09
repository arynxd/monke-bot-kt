package me.arynxd.monke.events

import dev.minn.jda.ktx.Embed
import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.CommandHandler
import me.arynxd.monke.handlers.CommandThreadHandler
import me.arynxd.monke.handlers.ConfigHandler
import me.arynxd.monke.handlers.GuildDataHandler
import me.arynxd.monke.util.DEFAULT_EMBED_COLOUR
import me.arynxd.monke.util.plurify
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.lang.IllegalArgumentException
import java.time.Instant

data class CommandPreprocessEvent(
    val monke: Monke,
    val message: Message,
    val guild: Guild,
    val user: User,
    val member: Member,
    val jda: JDA,
    val channel: TextChannel
) {
    constructor(event: GuildMessageReceivedEvent, monke: Monke) : this(
        monke,
        event.message,
        event.guild,
        event.author,
        event.member?: throw IllegalArgumentException("member was null"),
        event.jda,
        event.channel
    )

    constructor(event: GuildMessageUpdateEvent, monke: Monke) : this(
        monke,
        event.message,
        event.guild,
        event.author,
        event.member?: throw IllegalArgumentException("member was null"),
        event.jda,
        event.channel
    )
}

class JDAEvents(val monke: Monke) : EventListener {
    override fun onEvent(event: GenericEvent) {
        when (event) {
            is GuildMessageReceivedEvent  -> {
                if (event.author.isBot) {
                    return
                }

                monke.handlers[CommandHandler::class]
                    .handlePreprocessEvent(CommandPreprocessEvent(event, monke))
            }

            is GuildMessageUpdateEvent -> {
                if (event.author.isBot) {
                    return
                }

                monke.handlers[CommandHandler::class]
                    .handlePreprocessEvent(CommandPreprocessEvent(event, monke))
            }

            is GuildMessageDeleteEvent -> {
                monke.handlers[CommandThreadHandler::class]
                    .delete(event.messageIdLong, event.channel)
            }

            is GuildJoinEvent -> {
                val jda = event.jda
                val channel = jda.getTextChannelById(monke.handlers[ConfigHandler::class].config.logChannel) ?: return
                val serverCount = jda.guildCache.size()

                event.guild.retrieveOwner().queue {
                    channel.sendMessage(
                        Embed(
                            title = "Joined a new server!",
                            description = "Server: *${it.guild.name}* with owner **${
                                it.user.asTag
                            }**\n\n" +
                                    "Now at **${serverCount}** server${serverCount.plurify()}.",
                            color = DEFAULT_EMBED_COLOUR.rgb,
                            timestamp = Instant.now()
                        )
                    ).queue()
                }

                monke.handlers[GuildDataHandler::class].initGuild(event.guild.idLong)
            }

            is GuildLeaveEvent -> {
                val jda = event.jda
                val channel = jda.getTextChannelById(monke.handlers[ConfigHandler::class].config.logChannel) ?: return
                val serverCount = jda.guildCache.size()

                event.guild.retrieveOwner().queue {
                    channel.sendMessage(
                        Embed(
                            title = "Left a server :(",
                            description = "Server: *${it.guild.name}* with owner **${
                                it.user.asTag
                            }**\n\n" +
                                    "Now at **${serverCount}** server${serverCount.plurify()}.",
                            color = DEFAULT_EMBED_COLOUR.rgb,
                            timestamp = Instant.now()
                        )
                    ).queue()
                }
            }
         }
    }
}

