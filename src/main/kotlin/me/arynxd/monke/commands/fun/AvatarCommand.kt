package me.arynxd.monke.commands.`fun`

import dev.minn.jda.ktx.Embed
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentUser
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import net.dv8tion.jda.api.entities.User

@Suppress("UNUSED")
class AvatarCommand : Command(
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
) {

    override suspend fun run(event: CommandEvent) {
        val language = event.getLanguage()
        if (!event.isArgumentPresent(0)) {
            val forUser =
                TranslationHandler.getString(language, "command.avatar.response.avatar_for_user", event.user.asTag)
            event.sendEmbed(
                Embed(
                    title = forUser,
                    image = "${event.user.effectiveAvatarUrl}?size=2048"
                )
            )
            return
        }

        val user = event.getArgument<User>(0)
        val forUser = TranslationHandler.getString(language, "command.avatar.response.avatar_for_user", user.asTag)
        event.sendEmbed(
            Embed(
                title = forUser,
                image = "${user.effectiveAvatarUrl}?size=2048"
            )
        )
        return
    }
}