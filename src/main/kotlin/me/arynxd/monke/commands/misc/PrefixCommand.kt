package me.arynxd.monke.commands.misc

import dev.minn.jda.ktx.Embed
import me.arynxd.monke.handlers.GuildSettingsHandler
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent

@Suppress("UNUSED")
class PrefixCommand : Command(
    name = "prefix",
    description = "Gets and sets the prefix for this server.",
    category = CommandCategory.MISC,

    arguments = ArgumentConfiguration(listOf(
            ArgumentString(
                name = "prefix",
                description = "The new prefix. 5 characters or less.",
                required = false,
                type = ArgumentType.REGULAR,
                condition = { it.length <= 5 }
            )
        )
    ),

    ) {
    override suspend fun run(event: CommandEvent) {
        val cache = event.monke.handlers.get(GuildSettingsHandler::class.java).getCache(event.guildIdLong)
        val language = event.getLanguage()

        if (!event.isArgumentPresent(0)) {
            val prefixHere = TranslationHandler.getString(language, "command.prefix.response.prefix_here", cache.prefix)
            event.sendEmbed(Embed(
                title = prefixHere
            ))
            return
        }

        val prefix = event.getArgument<String>(0)

        val prefixAlready = TranslationHandler.getString(language, "command.prefix.response.prefix_already", prefix)
        val prefixNew = TranslationHandler.getString(language, "command.prefix.response.prefix_new", prefix)

        if (prefix == cache.prefix) {
            event.sendEmbed(Embed(
                title = prefixAlready
            ))
            return
        }

        cache.prefix = prefix
        event.sendEmbed(Embed(
            title = prefixNew
        ))
    }
}