package me.arynxd.monke.commands.`fun`

import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandMetaData
import me.arynxd.monke.objects.command.threads.CommandReply
import net.dv8tion.jda.api.entities.MessageEmbed

@Suppress("UNUSED")
class UwUCommand : Command(
    CommandMetaData(
        name = "uwu",
        description = "UwUfies your sentence.",
        category = CommandCategory.FUN,
        aliases = listOf("owo"),

        arguments = ArgumentConfiguration(
            ArgumentString(
                name = "text",
                description = "The text to UwUfy.",
                required = true,
                type = Argument.Type.VARARG,
            )
        )
    )
) {
    override fun runSync(event: CommandEvent) {
        event.replyAsync {
            val sentence = event.vararg<String>(0)
                .take(MessageEmbed.TEXT_MAX_LENGTH)
                .joinToString(separator = " ")
                .toCharArray()
                .map {
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
            event.thread.post(this)
        }
    }
}