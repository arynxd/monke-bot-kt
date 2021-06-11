package me.arynxd.monke.objects.argument

import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.SubCommand
import me.arynxd.monke.objects.translation.Language

abstract class Argument<T> {
    abstract val name: String
    abstract val description: String
    abstract val required: Boolean
    abstract val type: Type
    abstract val condition: (T) -> ArgumentResult<T>

    suspend fun verify(input: String, event: CommandEvent): ArgumentResult<T> {
        val conversion = convert(input, event)
        if (conversion.isError) {
            return conversion
        }

        return condition(conversion.data)
    }

    abstract suspend fun convert(input: String, event: CommandEvent): ArgumentResult<T>

    fun getDescription(language: Language, command: Command): String {
        val commandName = if (command is SubCommand) {
            command.parent.getName(language)
        }
        else {
            command.getName(language)
        }

        return translate {
            lang = language
            path = "command.$commandName.argument.$name.description"
        }
    }

    fun getName(language: Language, command: Command): String {
        val commandName = if (command is SubCommand) {
            command.parent.getName(language)
        }
        else {
            command.getName(language)
        }

        return translate {
            lang = language
            path = "command.$commandName.argument.$name.name"
        }
    }
}

enum class Type {
    VARARG,
    REGULAR
}

data class ArgumentResult<T>(
    private val success: T?,
    private val err: String? //TODO: treat this as a translation string later
) {
    val isError = err != null
    val isSuccess = success != null

    val data: T
        get() {
            if (success == null) {
                throw IllegalStateException("No data present")
            }
            return success
        }

    val error: String
        get() {
            if (err == null) {
                throw IllegalStateException("No error present")
            }
            return err
        }
}



