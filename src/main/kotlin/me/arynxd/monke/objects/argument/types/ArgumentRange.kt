package me.arynxd.monke.objects.argument.types

import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentResult
import me.arynxd.monke.objects.command.CommandEvent

class ArgumentRange (
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (Long) -> ArgumentResult<Long> = { ArgumentResult(it, null) },
    val upperBound: Long,
    val lowerBound: Long,

) : Argument<Long>() {
    override suspend fun convert(input: String, event: CommandEvent): ArgumentResult<Long> {
        val toInt = input.toLongOrNull()
            ?: return ArgumentResult(null, "command.argument.number.error.nan", arrayOf(input))

        if (toInt > upperBound || toInt < lowerBound) {
            return ArgumentResult(null, "command.argument.range.error.out_of_bounds", arrayOf(lowerBound, upperBound))
        }

        return ArgumentResult(toInt, null)
    }
}