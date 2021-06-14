package me.arynxd.monke.commands.misc.info

import dev.minn.jda.ktx.await
import me.arynxd.monke.handlers.translation.translate
import me.arynxd.monke.handlers.translation.translateAll
import me.arynxd.monke.objects.Emoji
import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.types.ArgumentGuild
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.util.parseDateTime
import net.dv8tion.jda.api.entities.Guild

@Suppress("UNUSED")
class InfoServerCommand(parent: Command) : SubCommand(
    parent,
    CommandMetaData(
        name = "server",
        description = "Shows information about a server.",
        category = CommandCategory.MISC,
        flags = listOf(CommandFlag.SUSPENDING),

        arguments = ArgumentConfiguration(
            ArgumentGuild(
                name = "server",
                description = "The server to show information for or nothing for the current server.",
                required = false,
                type = Argument.Type.REGULAR,
            )
        )
    )
) {
    override suspend fun runSuspend(event: CommandEvent) {
        val guild = event.argument(0, event.guild)
        val language = event.language

        val translations = translateAll(language) {
            part("command.info.keyword.information_for_server")

            part("command.info.keyword.is_partnered")
            part("command.info.keyword.is_public")
            part("command.info.keyword.is_verified")

            part("command.info.keyword.boost_count")
            part("command.info.keyword.member_count")
            part("command.info.keyword.created_at")

            part("command.info.keyword.emotes")
        }

        val informationFor = translations[0]

        val isPartnered = translations[1]
        val isVerified = translations[2]
        val isPublic = translations[3]

        val boostCount = translations[4]
        val memberCount = translations[5]
        val createdAt = translations[6]
        val emotes = translations[7]

        val description = """
            **$isPartnered**:  ${hasFeature(guild, "PARTNERED")}
            **$isVerified**:   ${hasFeature(guild, "VERIFIED")}
            **$isPublic**:     ${hasFeature(guild, "PUBLIC")}
            
            
            **$boostCount**:   ${guild.boostCount}
            **$emotes**:       ${getEmoteString(guild, language)}
            
            
            **$memberCount**:  ${guild.memberCount} / ${guild.maxMembers}
            **$createdAt**:    ${parseDateTime(guild.timeCreated)}
        """.trimIndent()

        event.reply {
            title("$informationFor ${guild.name}")
            type(CommandReply.Type.INFORMATION)
            description(description)
            thumbnail(guild.iconUrl)
            footer()
            event.thread.post(this)
        }
    }

    private fun hasFeature(guild: Guild, feature: String): String {
        return if (guild.features.contains(feature))
            Emoji.ENABLED.asChat
        else
            Emoji.DISABLED.asChat
    }

    private suspend fun getEmoteString(guild: Guild, language: Language): String {
        val emotes = guild.retrieveEmotes().await()
        if (emotes.isEmpty()) {
            return translate { lang = language; path = "command.info.keyword.no_emotes" }
        }

        val none = translate { lang = language; path = "keyword.none" }

        val animated =
            if (emotes.none { it.isAnimated })
                none
            else
                emotes.filter { it.isAnimated }
                    .joinToString(separator = " ") { it.asMention }

        val regular =
            if (emotes.none { !it.isAnimated })
                none
            else
                emotes.filter { !it.isAnimated }
                    .joinToString(separator = " ") { it.asMention }

        return translate {
            lang = language
            path = "command.info.child.server.response.emote"
            values = arrayOf(
                emotes.size,
                guild.maxEmotes,
                animated,
                regular
            )
        }
    }
}