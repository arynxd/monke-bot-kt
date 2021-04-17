package me.arynxd.monke.objects.command

import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.events.types.CommandEvent
import me.arynxd.monke.objects.translation.Language

abstract class SubCommand(
    val parent: Command,
    metaData: CommandMetaData,
) : Command(metaData) {
    override suspend fun runSuspend(event: CommandEvent) {
        //Placeholder method
    }

    override fun runSync(event: CommandEvent) {
        //Placeholder method
    }

    override fun getName(language: Language): String {
        return TranslationHandler.getString(language, "command.${parent.metaData.name}.child.${metaData.name}.name")
    }

    override fun getDescription(language: Language): String {
        return TranslationHandler.getString(
            language,
            "command.${parent.metaData.name}.child.${metaData.name}.description"
        )
    }
}