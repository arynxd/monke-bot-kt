package me.arynxd.monke.objects.argument.types

import me.arynxd.monke.handlers.CommandHandler
import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentResult
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandEvent

class ArgumentCommand(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (Command) -> ArgumentResult<Command> = { ArgumentResult(it, null) }

) : Argument<Command>() {

    override suspend fun convert(input: String, event: CommandEvent): ArgumentResult<Command> {
        val cmd = event.monke.handlers[CommandHandler::class].commandMap[input]

        return if (cmd == null) {
            ArgumentResult(null, "Command not found")
        }
        else {
            ArgumentResult(cmd, null)
        }
    }
}
