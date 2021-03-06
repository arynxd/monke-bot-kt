package me.arynxd.monke.commands.developer.plugin

import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentPlugin
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.plugins.LoadedPlugin

class PluginReloadCommand(parent: Command) : SubCommand(
    name = "reload",
    description = "Reloads a plugin.",
    category = CommandCategory.DEVELOPER,
    parent = parent,
    flags = listOf(CommandFlag.DEVELOPER_ONLY),
    arguments = ArgumentConfiguration(
        listOf(
            ArgumentPlugin(
                name = "plugin",
                description = "The plugin to reload.",
                required = true,
                type = ArgumentType.REGULAR
            )
        )
    )
) {
    override fun runSync(event: CommandEvent) {
        val plugin = event.getArgument<LoadedPlugin>(0)
        event.replyAsync {
            type(CommandReply.Type.SUCCESS)
            title("Reloaded plugin '${plugin.config.name}'")
            plugin.plugin.onDisable()
            plugin.plugin.onEnable(event.monke)
            plugin.isEnabled = true
            footer()
            send()
        }
    }
}