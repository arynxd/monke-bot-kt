package me.arynxd.monke.commands.misc.info

import dev.minn.jda.ktx.Embed
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentMember
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.SubCommand
import me.arynxd.monke.util.parseDateTime
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed

class InfoUserCommand(parent: Command) : SubCommand(
    name = "user",
    description = "Shows information about a user.",
    category = CommandCategory.MISC,
    parent = parent,

    arguments = ArgumentConfiguration(
        listOf(
            ArgumentMember(
                name = "member",
                description = "The member to show information for.",
                required = false,
                type = ArgumentType.REGULAR,
            )
        )
    )
) {

    override suspend fun run(event: CommandEvent) {
        val member = if (event.isArgumentPresent(0))
                        event.getArgument(0)
                     else
                         event.member

        val language = event.getLanguage()

        val information = TranslationHandler.getString(language, "command.info.keyword.information_for_user")
        val boosting = TranslationHandler.getString(language, "command.info.keyword.boosting_since")
        val notBoosting = TranslationHandler.getString(language, "command.info.keyword.not_boosting")
        val joinedAt = TranslationHandler.getString(language, "command.info.keyword.joined_at")
        val createdAt = TranslationHandler.getString(language, "command.info.keyword.created_at")
        val roles = TranslationHandler.getString(language, "command.info.keyword.roles")
        val noRoles = TranslationHandler.getString(language, "command.info.keyword.no_roles")

        event.sendEmbed(Embed(
            title = "$information: **" + member.user.asTag + "**",
            fields = listOf(
                MessageEmbed.Field(boosting,
                    if (member.timeBoosted == null) notBoosting else parseDateTime(member.timeBoosted),
                    true),
                MessageEmbed.Field(joinedAt, parseDateTime(member.timeJoined), true),
                MessageEmbed.Field(createdAt, parseDateTime(member.timeCreated), true),
                MessageEmbed.Field(roles, getCondensedRoles(member, noRoles), true),
            ),
            thumbnail = member.user.effectiveAvatarUrl
        ))
    }

    private fun getCondensedRoles(member: Member, error: String): String {
        if (member.roles.isEmpty()) {
            return error
        }

        if (member.roles.size < 5) {
            return member.roles.joinToString(separator = " ") { it.asMention }
        }

        return member.roles.subList(0, 5).joinToString(separator = " ") { it.asMention }
    }
}