package me.arynxd.monke.objects.argument.types

import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.events.types.command.CommandEvent

class ArgumentLong(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (Long) -> Boolean = { true },
) : Argument<Long>() {

    override suspend fun convert(input: String, event: CommandEvent) = input.toLongOrNull()
}

class ArgumentInt(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (Int) -> Boolean = { true },
) : Argument<Int>() {

    override suspend fun convert(input: String, event: CommandEvent) = input.toIntOrNull()
}

class ArgumentBoolean(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (Boolean) -> Boolean = { true },
) : Argument<Boolean>() {

    override suspend fun convert(input: String, event: CommandEvent): Boolean? {
        return when (input) {
            "no" -> false
            "false" -> false
            "n" -> false
            "0" -> false
            "f" -> false

            "yes" -> true
            "y" -> true
            "true" -> true
            "1" -> true
            "t" -> true

            else -> null
        }
    }
}

class ArgumentString(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (String) -> Boolean = { true },
) : Argument<String>() {
    override suspend fun convert(input: String, event: CommandEvent) = input
}