package me.arynxd.monke.commands.`fun`

import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.types.ArgumentRange
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandMetaData
import me.arynxd.monke.objects.command.threads.CommandReply
import kotlin.random.Random

@Suppress("UNUSED")
class RandomNumberCommand : Command(
    CommandMetaData(
        name = "rng",
        description = "Gives you a random number in the specified range.",
        category = CommandCategory.FUN,
        aliases = listOf("number"),

        arguments = ArgumentConfiguration(
            ArgumentRange(
                name = "number-one",
                description = "The first number in the range.",
                required = true,
                type = Argument.Type.REGULAR,
                upperBound = Long.MAX_VALUE,
                lowerBound = 0
            ),
            ArgumentRange(
                name = "number-two",
                description = "The second number in the range.",
                required = true,
                type = Argument.Type.REGULAR,
                upperBound = Long.MAX_VALUE,
                lowerBound = 0
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
                    translate {
                        lang = event.language
                        path = "command.rng.response.lower_>_upper"
                    }
                )
                footer()
                event.thread.post(this)
            }
            return
        }

        event.replyAsync {
            type(CommandReply.Type.SUCCESS)
            title(
                translate {
                    lang = event.language
                    path = "command.rng.response.success"
                    values = arrayOf(
                        Random.nextLong(lowerBound, upperBound)
                    )
                }
            )
            footer()
            event.thread.post(this)
        }
    }
}