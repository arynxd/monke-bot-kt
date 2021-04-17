package me.arynxd.monke.objects.argument.types

import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.events.types.CommandEvent
import me.arynxd.monke.objects.plugins.LoadedPlugin

class ArgumentPlugin(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: ArgumentType,
    override val condition: (LoadedPlugin) -> Boolean = { true },
) : Argument<LoadedPlugin>() {

    override suspend fun convert(input: String, event: CommandEvent): LoadedPlugin? {
        return event.monke.plugins.getByName(input)
    }
}