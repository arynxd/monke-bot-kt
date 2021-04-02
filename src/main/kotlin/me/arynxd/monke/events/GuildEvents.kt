package me.arynxd.monke.events

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.listener
import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.ConfigHandler
import me.arynxd.monke.util.DEFAULT_EMBED_COLOUR
import me.arynxd.monke.util.plurifyLong
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import java.time.Instant

fun Monke.guildEvents() {
    jda.listener<GuildJoinEvent> {
        val channel = jda.getTextChannelById(handlers.get(ConfigHandler::class).config.logChannel)?: return@listener
        val serverCount = jda.guildCache.size()
        channel.sendMessage(
            Embed(
                title = "Joined a new server!",
                description = "Server: *${it.guild.name}* with owner **${
                    it.guild.retrieveOwner().await().user.asTag
                }**\n\n" +
                        "Now at **${serverCount}** server${plurifyLong(serverCount)}.",
                color = DEFAULT_EMBED_COLOUR.rgb,
                timestamp = Instant.now()
            )
        ).queue()
    }

    jda.listener<GuildLeaveEvent> {
        val channel = jda.getTextChannelById(handlers.get(ConfigHandler::class).config.logChannel)?: return@listener
        val serverCount = jda.guildCache.size()

        channel.sendMessage(
            Embed(
                title = "Left a server :(",
                description = "Server: *${it.guild.name}* with owner **\n\n Now at **${serverCount}** server${
                    plurifyLong(serverCount)
                }.",
                color = DEFAULT_EMBED_COLOUR.rgb,
                timestamp = Instant.now()
            )
        ).queue()
    }
}