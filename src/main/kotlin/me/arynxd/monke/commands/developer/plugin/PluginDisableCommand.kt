package me.arynxd.monke.commands.developer.plugin

import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentPlugin
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.plugins.LoadedPlugin

class PluginDisableCommand(parent: Command) : SubCommand(
    name = "disable",
    description = "Disables a plugin.",
    category = CommandCategory.DEVELOPER,
    flags = listOf(CommandFlag.DEVELOPER_ONLY),
    parent = parent,
    arguments = ArgumentConfiguration(
        listOf(
            ArgumentPlugin(
                name = "plugin",
                description = "The plugin to disable.",
                required = true,
                type = ArgumentType.REGULAR
            )
        )
    )
) {
    override suspend fun run(event: CommandEvent) {
        val plugin = event.getArgument<LoadedPlugin>(0)
        if (!plugin.isEnabled) {
            event.reply {
                type(CommandReply.Type.EXCEPTION)
                title("Plugin '${plugin.config.name}' is already disabled.")
                footer()
                send()
            }
            return
        }

        event.reply {
            type(CommandReply.Type.SUCCESS)
            title("Disabled plugin '${plugin.config.name}'")
            plugin.plugin.onDisable()
            plugin.isEnabled = false
            footer()
            send()
        }
    }
}