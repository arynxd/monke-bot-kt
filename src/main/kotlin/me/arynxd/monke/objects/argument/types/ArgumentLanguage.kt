package me.arynxd.monke.objects.argument.types

import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentResult
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.translation.Language

class ArgumentLanguage(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (Language) -> ArgumentResult<Language> = { ArgumentResult(it, null) }
) : Argument<Language>() {

    override suspend fun convert(input: String, event: CommandEvent): ArgumentResult<Language> {
        val lang = Language.getLanguageOrNull(input)
        return if (lang == null) {
            ArgumentResult.ofFailure("command.argument.language.error.not_found", input)
        }
        else {
            ArgumentResult.ofSuccess(lang)
        }
    }
}