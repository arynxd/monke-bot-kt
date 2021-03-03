package me.arynxd.monke.commands.misc.info

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentServer
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.SubCommand
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.util.parseDateTime
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed

@Suppress("UNUSED")
class InfoServerCommand(parent: Command) : SubCommand(
    name = "server",
    description = "Shows information about a server.",
    category = CommandCategory.MISC,
    parent = parent,

    arguments = ArgumentConfiguration(
        listOf(
            ArgumentServer(
                name = "server",
                description = "The server to show information for or 'this' for the current server.",
                required = true,
                type = ArgumentType.REGULAR,
            )
        )
    )
) {
    override suspend fun run(event: CommandEvent) {
        val guild = event.getArgument<Guild>(0)
        val language = event.getLanguage()

        val informationFor = TranslationHandler.getString(language, "command.info.keyword.information_for_server")
        val isPartnered = TranslationHandler.getString(language, "command.info.keyword.is_partnered")
        val isVerified = TranslationHandler.getString(language, "command.info.keyword.is_verified")
        val isPublic = TranslationHandler.getString(language, "command.info.keyword.is_public")
        val boostCount = TranslationHandler.getString(language, "command.info.keyword.boost_count")
        val memberCount = TranslationHandler.getString(language, "command.info.keyword.member_count")
        val createdAt = TranslationHandler.getString(language, "command.info.keyword.created_at")
        val emotes = TranslationHandler.getString(language, "command.info.keyword.emotes")

        event.sendEmbed(Embed(
            title = "$informationFor **${guild.name}**",
            fields = listOf(
                MessageEmbed.Field(isPartnered, getFeature(guild, "PARTNERED", language), true),
                MessageEmbed.Field(isVerified, getFeature(guild, "VERIFIED", language), true),
                MessageEmbed.Field(isPublic, getFeature(guild, "PUBLIC", language), true),
                MessageEmbed.Field(boostCount, guild.boostCount.toString(), true),
                MessageEmbed.Field(memberCount, "${guild.memberCount} / ${guild.maxMembers}", true),
                MessageEmbed.Field(createdAt, parseDateTime(guild.timeCreated), true),
                MessageEmbed.Field(emotes, getEmoteString(guild, language), false),
            ),
            thumbnail = guild.iconUrl
        ))
    }

    private fun getFeature(guild: Guild, feature: String, language: Language): String {
        return if (guild.features.contains(feature))
                    TranslationHandler.getString(language, "keyword.yes")
               else
                    TranslationHandler.getString(language, "keyword.no")
    }

    private suspend fun getEmoteString(guild: Guild, language: Language): String {
        val emotes = guild.retrieveEmotes().await()
        if (emotes.isEmpty()) {
            return TranslationHandler.getString(language, "command.info.keyword.no_emotes")
        }

        val none = TranslationHandler.getString(language, "keyword.none")

        val animated = if (emotes.none { it.isAnimated }) none else emotes.filter { it.isAnimated }
            .joinToString(separator = " ") { it.asMention }
        val regular = if (emotes.none { !it.isAnimated }) none else emotes.filter { !it.isAnimated }
            .joinToString(separator = " ") { it.asMention }

        return TranslationHandler.getString(language, "command.info.child.server.response.emote",
                emotes.size,
                guild.maxEmotes,
                animated,
                regular
        )
    }
}