package me.arynxd.monke.commands.developer.plugin

import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.argument.types.ArgumentPlugin
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.events.types.command.CommandEvent
import me.arynxd.monke.objects.plugins.LoadedPlugin

class PluginReloadCommand(parent: Command) : SubCommand(
    parent,
    CommandMetaData(
        name = "reload",
        description = "Reloads a plugin.",
        category = CommandCategory.DEVELOPER,
        flags = listOf(CommandFlag.DEVELOPER_ONLY),
        arguments = ArgumentConfiguration(
            listOf(
                ArgumentPlugin(
                    name = "plugin",
                    description = "The plugin to reload.",
                    required = true,
                    type = Type.REGULAR
                )
            )
        )
    )
) {
    override fun runSync(event: CommandEvent) {
        val plugin = event.argument<LoadedPlugin>(0)
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