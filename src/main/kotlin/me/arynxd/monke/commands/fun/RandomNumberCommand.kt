package me.arynxd.monke.commands.`fun`

import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentLong
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.util.sendError
import me.arynxd.monke.util.sendSuccess
import kotlin.random.Random

@Suppress("UNUSED")
class RandomNumberCommand : Command(
    name = "rng",
    description = "Gives you a random number in the specified range.",
    category = CommandCategory.FUN,
    aliases = listOf("number"),

    arguments = ArgumentConfiguration(
        listOf(
            ArgumentLong(
                name = "number-one",
                description = "The first number in the range. Bigger than 0.",
                required = true,
                type = ArgumentType.REGULAR,
                condition = { it > 0 }
            ),

            ArgumentLong(
                name = "number-two",
                description = "The second number in the range. Bigger than 0.",
                required = true,
                type = ArgumentType.REGULAR,
                condition = { it > 0 }
            )
        )
    )
) {

    override suspend fun run(event: CommandEvent) {
        val lowerBound = event.getArgument<Long>(0)
        val upperBound = event.getArgument<Long>(1)

        if (lowerBound >= upperBound) {
            val error = TranslationHandler.getString(event.getLanguage(), "command.rng.response.lower_>_upper")
            sendError(event.message, error)
            return
        }

        sendSuccess(event.message, Random.nextLong(lowerBound, upperBound).toString())
    }
}