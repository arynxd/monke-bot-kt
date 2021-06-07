package me.arynxd.monke.objects.argument.types

import me.arynxd.monke.handlers.CommandHandler
import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandEvent

class ArgumentCommand(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (Command) -> Boolean = { true }

) : Argument<Command>() {

    override suspend fun convert(input: String, event: CommandEvent) =
        event.monke.handlers[CommandHandler::class].commandMap[input]
}
