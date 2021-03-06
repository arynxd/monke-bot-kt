package me.arynxd.monke.commands.misc.info

import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentMember
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.util.parseDateTime
import net.dv8tion.jda.api.entities.Member

class InfoUserCommand(parent: Command) : SubCommand(
    name = "user",
    description = "Shows information about a user.",
    category = CommandCategory.MISC,
    flags = listOf(CommandFlag.ASYNC),
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

    override suspend fun runSuspend(event: CommandEvent) {
        val member = event.getArgument(0, event.member)

        val language = event.getLanguage()

        val information = TranslationHandler.getString(language, "command.info.keyword.information_for_user")
        val boosting = TranslationHandler.getString(language, "command.info.keyword.boosting_since")
        val notBoosting = TranslationHandler.getString(language, "command.info.keyword.not_boosting")
        val joinedAt = TranslationHandler.getString(language, "command.info.keyword.joined_at")
        val createdAt = TranslationHandler.getString(language, "command.info.keyword.created_at")
        val roles = TranslationHandler.getString(language, "command.info.keyword.roles")
        val noRoles = TranslationHandler.getString(language, "command.info.keyword.no_roles")

        event.reply {
            type(CommandReply.Type.INFORMATION)
            title("$information: **${member.user.asTag}**")
            field(boosting, parseDateTime(member.timeBoosted) ?: notBoosting, true)
            field(joinedAt, parseDateTime(member.timeJoined), true)
            field(createdAt, parseDateTime(member.timeCreated), true)
            field(roles, getCondensedRoles(member, noRoles), true)

            footer()
            thumbnail(member.user.effectiveAvatarUrl)
            send()
        }
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