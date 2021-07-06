package me.arynxd.monke.commands.developer

import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.types.ArgumentCommand
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.util.equalsIgnoreCase

@Suppress("UNUSED")
class AdminCommand : Command(
    CommandMetaData(
        name = "admin",
        description = "Controls the bot's internals",
        category = CommandCategory.DEVELOPER,
        flags = listOf(CommandFlag.DEVELOPER_ONLY, CommandFlag.SUSPENDING, CommandFlag.DISABLED),

        arguments = ArgumentConfiguration(
            ArgumentString(
                name = "action",
                description = "The action to perform. (enable, disable, reload, info)",
                required = true,
                type = Argument.Type.REGULAR
            ),
            ArgumentString(
                name = "type",
                description = "The type to perform the action on. (emoji, command)",
                required = true,
                type = Argument.Type.REGULAR
            ),
            ArgumentString(
                name = "target",
                description = "The target to perform the action on.",
                required = true,
                type = Argument.Type.REGULAR
            )
        )
    )
) {
    @Suppress("UNUSED_VARIABLE")
    override suspend fun runSuspend(event: CommandEvent) {
        val action = event.argument<String>(0)
        val type = event.argument<String>(1)
        val target = event.argument<String>(2)
        TODO("Impl this") //TODO
    }
}