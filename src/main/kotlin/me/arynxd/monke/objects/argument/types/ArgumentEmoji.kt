package me.arynxd.monke.objects.argument.types

import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentResult
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.util.classes.EmojiValidator

class ArgumentEmoji(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (String) -> ArgumentResult<String> = { ArgumentResult(it, null) }

) : Argument<String>() {
    override suspend fun convert(input: String, event: CommandEvent): ArgumentResult<String> =
        if (EmojiValidator.unicodeExists(input)) ArgumentResult(input, null)
        else ArgumentResult(null, "Emoji not found")
}