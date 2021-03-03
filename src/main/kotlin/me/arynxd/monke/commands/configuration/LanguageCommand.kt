package me.arynxd.monke.commands.configuration

import dev.minn.jda.ktx.Embed
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentLanguage
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandFlag
import me.arynxd.monke.objects.translation.Language

@Suppress("UNUSED")
class LanguageCommand : Command(
    name = "language",
    description = "Gets and sets the language for this server.",
    category = CommandCategory.CONFIGURATION,

    arguments = ArgumentConfiguration(
        listOf(
            ArgumentLanguage(
                name = "language",
                description = "The new language.",
                required = false,
                type = ArgumentType.REGULAR,
            )
        )
    )
) {
    override suspend fun run(event: CommandEvent) {
        val cache = event.getSettingsCache()
        val language = cache.language

        if (!event.isArgumentPresent(0)) {
            event.sendEmbed(Embed(
                title = TranslationHandler.getString(language, "command.language.response.get_response", language.commonName)
            ))
            return
        }

        val newLanguage = event.getArgument<Language>(0)
        if (language == newLanguage) {
            event.sendEmbed(Embed(
                title = TranslationHandler.getString(language, "command.language.response.exists_response", language.commonName)
            ))
            return
        }

        event.sendEmbed(Embed(
            title = TranslationHandler.getString(newLanguage, "command.language.response.set_response", newLanguage.commonName)
        ))

        cache.language = newLanguage
    }
}