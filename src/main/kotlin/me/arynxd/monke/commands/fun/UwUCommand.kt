package me.arynxd.monke.commands.`fun`

import dev.minn.jda.ktx.Embed
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent

@Suppress("UNUSED")
class UwUCommand : Command(
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
                condition = { it.isNotBlank() }
            )
        )
    ),

    ) {
    override suspend fun run(event: CommandEvent) {
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

        event.sendEmbed(
            Embed(
                description = sentence
            )
        )
    }
}