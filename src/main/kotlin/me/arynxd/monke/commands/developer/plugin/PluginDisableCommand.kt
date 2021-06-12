package me.arynxd.monke.commands.developer.plugin

import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.types.ArgumentPlugin
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.objects.plugins.LoadedPlugin

class PluginDisableCommand(parent: Command) : SubCommand(
    parent,
    CommandMetaData(
        name = "disable",
        description = "Disables a plugin.",
        category = CommandCategory.DEVELOPER,
        flags = listOf(CommandFlag.DEVELOPER_ONLY),
        arguments = ArgumentConfiguration(
            ArgumentPlugin(
                name = "plugin",
                description = "The plugin to disable.",
                required = true,
                type = Argument.Type.REGULAR
            )
        )
    )
) {
    override fun runSync(event: CommandEvent) {
        val plugin = event.argument<LoadedPlugin>(0)
        if (!plugin.isEnabled) {
            event.replyAsync {
                type(CommandReply.Type.EXCEPTION)
                title("Plugin '${plugin.config.name}' is already disabled.")
                footer()
                send()
            }
            return
        }

        event.replyAsync {
            type(CommandReply.Type.SUCCESS)
            title("Disabled plugin '${plugin.config.name}'")
            plugin.plugin.onDisable()
            plugin.isEnabled = false
            footer()
            send()
        }
    }
}