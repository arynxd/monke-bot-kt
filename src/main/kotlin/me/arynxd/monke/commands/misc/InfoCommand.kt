package me.arynxd.monke.commands.misc

import me.arynxd.monke.commands.misc.info.InfoBotCommand
import me.arynxd.monke.commands.misc.info.InfoServerCommand
import me.arynxd.monke.commands.misc.info.InfoUserCommand
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.argument.types.ArgumentMember
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandMetaData

@Suppress("UNUSED")
class InfoCommand : Command(
    CommandMetaData(
        name = "info",
        description = "Shows information about various things.",
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
    init {
        super.children.addAll(
            listOf(
                InfoUserCommand(this),
                InfoServerCommand(this),
                InfoBotCommand(this)
            )
        )
    }

    override fun runSync(event: CommandEvent) {
        event.command.children[0].runSync(event)
    }
}