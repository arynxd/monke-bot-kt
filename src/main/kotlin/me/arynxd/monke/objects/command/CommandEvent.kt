package me.arynxd.monke.objects.command

import dev.minn.jda.ktx.Embed
import me.arynxd.monke.Monke
import me.arynxd.monke.events.GuildMessageEvent
import me.arynxd.monke.handlers.ConfigHandler
import me.arynxd.monke.handlers.GuildSettingsHandler
import me.arynxd.monke.objects.database.GuildSettings
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.util.DEFAULT_EMBED_COLOUR
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import java.time.Instant

class CommandEvent(
    val event: GuildMessageEvent,
    val command: Command,
    val args: MutableList<Any>,
    val monke: Monke
) {

    val jda: JDA = event.jda
    val channel = event.channel
    val user: User = event.user
    val messageChannel: MessageChannel = event.channel
    val guild = event.guild
    val message = event.message
    val member = event.member
    val selfMember = event.guild.selfMember
    val guildIdLong = event.guild.idLong

    fun sendEmbed(embed: MessageEmbed) {
        val user = message.author

        message.reply(
            Embed(
                title = embed.title,
                description = embed.description,
                image = embed.image?.url,
                thumbnail = embed.thumbnail?.url,
                fields = embed.fields,
                color = DEFAULT_EMBED_COLOUR.rgb,
                footerText = embed.footer?.text ?: user.name,
                timestamp = if (embed.footer?.text == null) Instant.now() else null,
                footerIcon = user.effectiveAvatarUrl
            )
        ).mentionRepliedUser(false).queue()
    }

    fun isDeveloper(): Boolean {
        return monke.handlers.get(ConfigHandler::class).config.developers.contains(user.id)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getArgument(indie: Int): T {
        return args[indie] as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getVararg(start: Int): MutableList<T> {
        return args
            .subList(start, args.size)
            .map { it as T }
            .toMutableList()
    }

    fun isArgumentPresent(indie: Int): Boolean {
        return indie < args.size
    }

    fun getPrefix(): String {
        return monke.handlers.get(GuildSettingsHandler::class).getCache(guildIdLong).prefix
    }

    fun getLanguage(): Language {
        return monke.handlers.get(GuildSettingsHandler::class).getCache(guildIdLong).language
    }

    fun getSettingsCache(): GuildSettings {
        return monke.handlers.get(GuildSettingsHandler::class).getCache(guildIdLong)
    }
}