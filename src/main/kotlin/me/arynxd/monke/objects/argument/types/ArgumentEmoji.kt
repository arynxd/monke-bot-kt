package me.arynxd.monke.objects.argument.types

import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.util.classes.EmojiValidator

class ArgumentEmoji(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (String) -> Boolean = { true }

) : Argument<String>() {
    override suspend fun convert(input: String, event: CommandEvent) =
        if (EmojiValidator.unicodeExists(input)) input
        else null
}