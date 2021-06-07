package me.arynxd.monke.objects.argument.types

import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.translation.Language

class ArgumentLanguage(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (Language) -> Boolean = { true },
) : Argument<Language>() {

    override suspend fun convert(input: String, event: CommandEvent) = Language.getLanguageOrNull(input)
}