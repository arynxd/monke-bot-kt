package me.arynxd.monke.objects.argument.types

import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentResult
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.command.CommandEvent

class ArgumentLong(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (Long) -> ArgumentResult<Long> = { ArgumentResult(it, null) }
) : Argument<Long>() {
    override suspend fun convert(input: String, event: CommandEvent): ArgumentResult<Long> {
        val long = input.toLongOrNull()
        return if (long == null) {
            ArgumentResult(null, "Not a number")
        }
        else {
            ArgumentResult(long, null)
        }
    }
}

class ArgumentInt(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (Int) -> ArgumentResult<Int> = { ArgumentResult(it, null) }
) : Argument<Int>() {
    override suspend fun convert(input: String, event: CommandEvent): ArgumentResult<Int> {
        val int = input.toIntOrNull()
        return if (int == null) {
            ArgumentResult(null, "Not a number")
        }
        else {
            ArgumentResult(int, null)
        }
    }
}

class ArgumentBoolean(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (Boolean) -> ArgumentResult<Boolean> = { ArgumentResult(it, null) }
) : Argument<Boolean>() {

    override suspend fun convert(input: String, event: CommandEvent): ArgumentResult<Boolean> {
        val res = when (input) {
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
        } ?: return ArgumentResult(null, "Not true or false")
        return ArgumentResult(res, null)
    }
}

class ArgumentString(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (String) -> ArgumentResult<String> = { ArgumentResult(it, null) }
) : Argument<String>() {
    override suspend fun convert(input: String, event: CommandEvent) = ArgumentResult(input, null)
}