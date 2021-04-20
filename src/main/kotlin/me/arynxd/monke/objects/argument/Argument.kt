package me.arynxd.monke.objects.argument

import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.SubCommand
import me.arynxd.monke.objects.events.types.command.CommandEvent
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

enum class Type {
    VARARG,
    REGULAR
}