package me.arynxd.monke.commands.`fun`

import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandMetaData
import me.arynxd.monke.objects.command.CommandReply
import me.arynxd.monke.objects.events.types.command.CommandEvent
import net.dv8tion.jda.api.entities.MessageEmbed

@Suppress("UNUSED")
class UwUCommand : Command(
    CommandMetaData(
        name = "uwu",
        description = "UwUfies your sentence.",
        category = CommandCategory.FUN,
        aliases = listOf("owo"),

        arguments = ArgumentConfiguration(
            listOf(
                ArgumentString(
                    name = "text",
                    description = "The text to UwUfy.",
                    required = true,
                    type = ArgumentType.VARARG,
                    condition = { it.isNotBlank() && it.length < MessageEmbed.TEXT_MAX_LENGTH }
                )
            )
        )
    )
) {
    override fun runSync(event: CommandEvent) {
        event.replyAsync {
            val sentence = event.getVararg<String>(0)
                .joinToString(separator = " ")
                .toCharArray().map {
                    when (it) {
                        'r', 'l' -> "w"
                        'o' -> "wo"
                        'a' -> "aw"
                        'i' -> "iw"
                        else -> it
                    }
                }.joinToString(separator = "")

            type(CommandReply.Type.SUCCESS)
            description(sentence)
            footer()
            send()
        }
    }
}