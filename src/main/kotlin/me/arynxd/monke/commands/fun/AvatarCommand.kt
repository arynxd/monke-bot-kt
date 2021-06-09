package me.arynxd.monke.commands.`fun`

import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.argument.types.ArgumentUser
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandMetaData
import me.arynxd.monke.objects.command.threads.CommandReply

@Suppress("UNUSED")
class AvatarCommand : Command(
    CommandMetaData(
        name = "avatar",
        description = "Shows the avatar of a user.",
        category = CommandCategory.FUN,
        aliases = listOf("avi", "pfp", "av"),

        arguments = ArgumentConfiguration(
            ArgumentUser(
                name = "user",
                description = "The user to get the avatar for.",
                required = false,
                type = Type.REGULAR,
            )
        )
    )
) {

    override fun runSync(event: CommandEvent) {
        val language = event.language
        val user = event.argument(0, event.user)

        event.replyAsync {
            type(CommandReply.Type.INFORMATION)
            title(
                translate {
                    lang = language
                    path = "command.avatar.response.avatar_for_user"
                    values = arrayOf(user.asTag)
                }
            )
            image(user.effectiveAvatarUrl, 2048)
            footer()
            event.thread.post(this)
        }
    }
}