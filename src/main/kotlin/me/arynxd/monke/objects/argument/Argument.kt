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
    abstract val condition: (T) -> Boolean

    suspend fun verify(input: String, event: CommandEvent): T? { // Null on invalid
        val conversion = convert(input, event) ?: return null
        return if (condition.invoke(conversion)) conversion else null
    }

    abstract suspend fun convert(input: String, event: CommandEvent): T? // Null on invalid

    fun getDescription(language: Language, command: Command): String {
        val commandName =
            if (command is SubCommand)
                "${command.parent.getName(language)}.child.${command.getName(language)}"
            else
                command.getName(language)
        return translate(language, "command.$commandName.argument.$name.description")
    }

    fun getName(language: Language, command: Command): String {
        val commandName =
            if (command is SubCommand)
                "${command.parent.getName(language)}.child.${command.getName(language)}"
            else
                command.getName(language)
        return translate(language, "command.$commandName.argument.$name.name")
    }
}

data class ArgumentBuilder<T>(
    var name: String? = null,
    var description: String? = null,
    var required: Boolean? = null,
    var type: Type? = null,
    var condition: (T) -> Boolean = { true }
) {
    fun verify() {
        when {
            name == null -> throw IllegalArgumentException("name was null")
            description == null -> throw IllegalArgumentException("description was null")
            required == null -> throw IllegalArgumentException("required was null")
            type == null -> throw IllegalArgumentException("type was null")
        }
    }
}

enum class Type {
    VARARG,
    REGULAR
}