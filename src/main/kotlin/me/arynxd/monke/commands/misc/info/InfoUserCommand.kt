package me.arynxd.monke.commands.misc.info

import me.arynxd.monke.handlers.translation.translateAll
import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.types.ArgumentMember
import me.arynxd.monke.objects.command.*
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
                type = Argument.Type.REGULAR,
            )
        )
    )
) {

    override fun runSync(event: CommandEvent) {
        val member = event.argument(0, event.member)

        val language = event.language

        val translations = translateAll(language) {
            part("command.info.keyword.information_for_user")
            part("command.info.keyword.boosting_since")
            part("command.info.keyword.not_boosting")

            part("command.info.keyword.joined_at")
            part("command.info.keyword.created_at")
            part("command.info.keyword.roles")
            part("command.info.keyword.no_roles")
        }

        val information = translations[0]
        val boosting = translations[1]
        val notBoosting = translations[2]
        val joinedAt = translations[3]
        val createdAt = translations[4]
        val roles = translations[5]
        val noRoles = translations[6]

        event.replyAsync {
            type(CommandReply.Type.INFORMATION)
            title("$information: **${member.user.asTag}**")
            field(boosting, parseDateTime(member.timeBoosted) ?: notBoosting, true)
            field(joinedAt, parseDateTime(member.timeJoined), true)
            field(createdAt, parseDateTime(member.timeCreated), true)
            field(roles, getCondensedRoles(member, noRoles), true)

            footer()
            thumbnail(member.user.effectiveAvatarUrl)
            event.thread.post(this)
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