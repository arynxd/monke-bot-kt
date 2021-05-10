package me.arynxd.monke.commands.`fun`

import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.argument.types.ArgumentLong
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandMetaData
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.objects.command.CommandEvent
import kotlin.random.Random

@Suppress("UNUSED")
class RandomNumberCommand : Command(
    CommandMetaData(
        name = "rng",
        description = "Gives you a random number in the specified range.",
        category = CommandCategory.FUN,
        aliases = listOf("number"),

        arguments = ArgumentConfiguration(
            ArgumentLong(
                name = "number-one",
                description = "The first number in the range. Bigger than 0.",
                required = true,
                type = Type.REGULAR,
                condition = { it > 0 }
            ),
            ArgumentLong(
                name = "number-two",
                description = "The second number in the range. Bigger than 0.",
                required = true,
                type = Type.REGULAR,
                condition = { it > 0 }
            )
        )
    )
) {

    override fun runSync(event: CommandEvent) {
        val lowerBound = event.argument<Long>(0)
        val upperBound = event.argument<Long>(1)

        if (lowerBound >= upperBound) {
            event.replyAsync {
                type(CommandReply.Type.EXCEPTION)
                title(
                    translate(
                        language = event.language(),
                        key = "command.rng.response.lower_>_upper"
                    )
                )
                footer()
                send()
            }
            return
        }

        event.replyAsync {
            type(CommandReply.Type.SUCCESS)
            title("I choose ${Random.nextLong(lowerBound, upperBound)}!")
            footer()
            send()
        }
    }
}