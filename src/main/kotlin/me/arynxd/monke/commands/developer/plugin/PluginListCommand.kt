package me.arynxd.monke.commands.developer.plugin

import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.command.CommandEvent

class PluginListCommand(parent: Command) : SubCommand(
    parent,
    CommandMetaData(
        name = "list",
        description = "Lists all loaded plugins.",
        category = CommandCategory.DEVELOPER,
        flags = listOf(CommandFlag.DEVELOPER_ONLY)
    )
) {
    override fun runSync(event: CommandEvent) {
        event.replyAsync {
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