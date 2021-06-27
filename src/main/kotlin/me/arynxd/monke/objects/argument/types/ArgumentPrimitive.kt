package me.arynxd.monke.objects.argument.types

import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentResult
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
            ArgumentResult.ofFailure("command.argument.number.error.nan", input)
        }
        else {
            ArgumentResult.ofSuccess(long)
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
            ArgumentResult.ofFailure("command.argument.number.error.nan", input)
        }
        else {
            ArgumentResult.ofSuccess(int)
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
        } ?: return ArgumentResult.ofFailure("command.argument.boolean.error.invalid", input)
        return ArgumentResult.ofSuccess(res)
    }
}

class ArgumentString(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (String) -> ArgumentResult<String> = { ArgumentResult(it, null) }
) : Argument<String>() {
    override suspend fun convert(input: String, event: CommandEvent): ArgumentResult<String> {
        return if (input.isBlank()) {
            ArgumentResult(null, "command.argument.string.error.empty")
        }
        else {
            ArgumentResult(input, null)
        }
    }
}