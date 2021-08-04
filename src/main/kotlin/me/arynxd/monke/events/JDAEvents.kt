package me.arynxd.monke.events

import dev.minn.jda.ktx.Embed
import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.ConfigHandler
import me.arynxd.monke.handlers.GuildDataHandler
import me.arynxd.monke.objects.events.types.command.CommandPreprocessEvent
import me.arynxd.monke.util.DEFAULT_EMBED_COLOUR
import me.arynxd.monke.util.plurifyLong
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.time.Instant

class JDAEvents(val monke: Monke) : ListenerAdapter() {
    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.author.isBot) {
            return
        }

        monke.eventProcessor.fireEvent(CommandPreprocessEvent(event, monke))
    }

    override fun onGuildMessageUpdate(event: GuildMessageUpdateEvent) {
        if (event.author.isBot) {
            return
        }

        monke.eventProcessor.fireEvent(CommandPreprocessEvent(event, monke))
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

        monke.handlers.get(GuildDataHandler::class).initGuild(event.guild)
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
