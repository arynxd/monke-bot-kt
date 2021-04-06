package me.arynxd.monke.commands.developer

import me.arynxd.monke.commands.developer.plugin.*
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandFlag

//TODO: Add translation for the child commands
@Suppress("UNUSED")
class PluginCommand : Command(
    name = "plugin",
    description = "Controls the currently running plugins.",
    category = CommandCategory.DEVELOPER,
    flags = listOf(CommandFlag.DEVELOPER_ONLY)
) {
    init {
        super.children.addAll(
            listOf(
                PluginEnableCommand(this),
                PluginDisableCommand(this),
                PluginReloadCommand(this),
                PluginListCommand(this),
                PluginRefreshCommand(this)
            )
        )
    }

    override suspend fun run(event: CommandEvent) {
        event.command.children[3].run(event)
    }
}