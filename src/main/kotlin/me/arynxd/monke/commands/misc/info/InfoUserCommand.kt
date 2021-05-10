package me.arynxd.monke.commands.misc.info

import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.argument.types.ArgumentMember
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.util.parseDateTime
import net.dv8tion.jda.api.entities.Member

class InfoUserCommand(parent: Command) : SubCommand(
    parent,
    CommandMetaData(
        name = "user",
        description = "Shows information about a user.",
        category = CommandCategory.MISC,

        arguments = ArgumentConfiguration(
            ArgumentMember(
                name = "member",
                description = "The member to show information for.",
                required = false,
                type = Type.REGULAR,
            )
        )
    )
) {

    override fun runSync(event: CommandEvent) {
        val member = event.argument(0, event.member)

        val language = event.language()

        val information = translate(language, "command.info.keyword.information_for_user")
        val boosting = translate(language, "command.info.keyword.boosting_since")
        val notBoosting = translate(language, "command.info.keyword.not_boosting")
        val joinedAt = translate(language, "command.info.keyword.joined_at")
        val createdAt = translate(language, "command.info.keyword.created_at")
        val roles = translate(language, "command.info.keyword.roles")
        val noRoles = translate(language, "command.info.keyword.no_roles")

        event.replyAsync {
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