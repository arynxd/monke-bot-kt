package me.arynxd.monke.commands.developer.plugin

import me.arynxd.monke.objects.command.*

class PluginRefreshCommand(parent: Command) : SubCommand(
    name = "refresh",
    description = "Reloads all plugins from disk.",
    category = CommandCategory.DEVELOPER,
    parent = parent,
    flags = listOf(CommandFlag.DEVELOPER_ONLY)
) {
    override suspend fun run(event: CommandEvent) {
        event.monke.plugins.reload()
        event.reply {
            type(CommandReply.Type.SUCCESS)
            title("Reloaded all plugins!")
            footer()
            send()
        }
    }
}