package me.arynxd.monke.commands.developer.plugin

import me.arynxd.monke.objects.command.*

class PluginListCommand(parent: Command) : SubCommand(
    name = "enable",
    description = "Enables a plugin.",
    category = CommandCategory.DEVELOPER,
    parent = parent,
    flags = listOf(CommandFlag.DEVELOPER_ONLY)
) {
    override suspend fun run(event: CommandEvent) {
        event.reply {
            val plugins = event.monke.plugins.getPluginList().let {
                if (it.isBlank()) {
                    return@let "No plugins loaded"
                }
                else {
                    return@let it
                }
            }

            type(CommandReply.Type.INFORMATION)
            title("Currently loaded plugins")
            description(plugins)
            footer()
            send()
        }
    }
}