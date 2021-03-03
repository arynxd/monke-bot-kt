package me.arynxd.monke.commands.misc

import me.arynxd.monke.commands.misc.info.InfoBotCommand
import me.arynxd.monke.commands.misc.info.InfoServerCommand
import me.arynxd.monke.commands.misc.info.InfoUserCommand
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentMember
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent

@Suppress("UNUSED")
class InfoCommand : Command(
    name = "info",
    description = "Shows information about various things.",
    category = CommandCategory.MISC,

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
    init {
        super.children.addAll(listOf(
            InfoUserCommand(this),
            InfoServerCommand(this),
            InfoBotCommand(this)
        ))
    }

    override suspend fun run(event: CommandEvent) {
        event.command.children[0].run(event)
    }
}