package me.arynxd.monke.commands.`fun`

import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentUser
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandMetaData
import me.arynxd.monke.objects.events.types.CommandEvent
import me.arynxd.monke.objects.command.CommandReply

@Suppress("UNUSED")
class AvatarCommand : Command(
    CommandMetaData(
        name = "avatar",
        description = "Shows the avatar of a user.",
        category = CommandCategory.FUN,
        aliases = listOf("avi", "pfp", "av"),

        arguments = ArgumentConfiguration(
            listOf(
                ArgumentUser(
                    name = "user",
                    description = "The user to get the avatar for.",
                    required = false,
                    type = ArgumentType.REGULAR,
                )
            )
        )
    )
) {

    override fun runSync(event: CommandEvent) {
        val language = event.getLanguage()
        val user = if (event.isArgumentPresent(0)) event.getArgument(0) else event.user

        event.replyAsync {
            type(CommandReply.Type.INFORMATION)
            title(
                TranslationHandler.getString(
                    language = language,
                    key = "command.avatar.response.avatar_for_user",
                    user.asTag
                )
            )
            image(user.effectiveAvatarUrl, 2048)
            footer()
            send()
        }
    }
}