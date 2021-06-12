package me.arynxd.monke.objects.argument.types

import me.arynxd.monke.handlers.CommandHandler
import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentResult
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
        val help = translate {
            lang = event.language
            path = "command.help.name"
        }
        return if (cmd == null) {
            ArgumentResult(null, "command.argument.command.error.not_found", arrayOf(input, help))
        }
        else {
            ArgumentResult(cmd, null)
        }
    }
}
