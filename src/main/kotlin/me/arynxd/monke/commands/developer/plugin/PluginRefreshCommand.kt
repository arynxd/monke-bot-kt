package me.arynxd.monke.commands.developer.plugin

import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.command.precondition.impl.DeveloperPrecondition
import me.arynxd.monke.objects.command.threads.CommandReply

class PluginRefreshCommand(parent: Command) : SubCommand(
    parent,
    CommandMetaData(
        name = "refresh",
        description = "Reloads all plugins from disk.",
        category = CommandCategory.DEVELOPER,
        preconditions = listOf(DeveloperPrecondition())
    )
) {
    override fun runSync(event: CommandEvent) {
        event.monke.plugins.reload()
        event.replyAsync {
            type(CommandReply.Type.SUCCESS)
            title("Reloaded all plugins!")
            footer()
            send()
        }
    }
}