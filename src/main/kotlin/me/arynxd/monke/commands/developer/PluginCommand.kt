package me.arynxd.monke.commands.developer

import me.arynxd.monke.commands.developer.plugin.*
import me.arynxd.monke.objects.command.*

//TODO: Add translation for the child commands
@Suppress("UNUSED")
class PluginCommand : Command(
    CommandMetaData(
        name = "plugin",
        description = "Controls the currently running plugins.",
        category = CommandCategory.DEVELOPER,
        flags = listOf(CommandFlag.DEVELOPER_ONLY)
    )
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

    override fun runSync(event: CommandEvent) {
        event.command.children[3].runSync(event)
    }
}