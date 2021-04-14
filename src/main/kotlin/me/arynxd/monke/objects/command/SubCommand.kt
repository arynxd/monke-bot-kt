package me.arynxd.monke.objects.command

import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.translation.Language
import net.dv8tion.jda.api.Permission

abstract class SubCommand(
    val parent: Command,
    name: String,
    description: String,
    category: CommandCategory,

    flags: List<CommandFlag> = emptyList(),
    arguments: ArgumentConfiguration = ArgumentConfiguration(emptyList()),

    memberPermissions: List<Permission> = emptyList(),
    botPermissions: List<Permission> = emptyList(),
) : Command(
    name = name,
    description = description,
    category = category,

    aliases = emptyList(),
    flags = flags,
    arguments = arguments,
    memberPermissions = memberPermissions,
    botPermissions = botPermissions,

    ) {
    override suspend fun runSuspend(event: CommandEvent) {
        //Placeholder method
    }

    override fun runSync(event: CommandEvent) {
        //Placeholder method
    }

    override fun getName(language: Language): String {
        return TranslationHandler.getString(language, "command.${parent.name}.child.$name.name")
    }

    override fun getDescription(language: Language): String {
        return TranslationHandler.getString(language, "command.${parent.name}.child.$name.description")
    }
}