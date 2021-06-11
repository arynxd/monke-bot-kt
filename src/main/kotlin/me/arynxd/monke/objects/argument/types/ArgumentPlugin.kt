package me.arynxd.monke.objects.argument.types

import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentResult
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.plugins.LoadedPlugin

class ArgumentPlugin(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (LoadedPlugin) -> ArgumentResult<LoadedPlugin> = { ArgumentResult(it, null) },
) : Argument<LoadedPlugin>() {

    override suspend fun convert(input: String, event: CommandEvent): ArgumentResult<LoadedPlugin> {
        val plugin = event.monke.plugins.getByName(input)
        return if (plugin == null) {
            ArgumentResult(null, "command.argument.plugin.error.not_found", arrayOf(input))
        }
        else {
            ArgumentResult(plugin, null)
        }
    }
}