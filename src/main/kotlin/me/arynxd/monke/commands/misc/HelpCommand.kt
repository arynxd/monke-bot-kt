package me.arynxd.monke.commands.misc

import dev.minn.jda.ktx.Embed
import me.arynxd.monke.handlers.CommandHandler
import me.arynxd.monke.handlers.PaginationHandler
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.Paginator
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentCommand
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.SubCommand
import me.arynxd.monke.util.DEFAULT_EMBED_COLOUR
import net.dv8tion.jda.api.entities.MessageEmbed


@Suppress("UNUSED")
class HelpCommand : Command(
    name = "help",
    description = "Shows help menu, or help for a specific command.",
    category = CommandCategory.MISC,
    aliases = listOf("?", "commands"),

    arguments = ArgumentConfiguration(
        listOf(
            ArgumentCommand(
                name = "command",
                description = "The command to show help for.",
                required = false,
                type = ArgumentType.REGULAR,
            )
        )
    ),

    ) {
    override suspend fun run(event: CommandEvent) {
        val prefix = event.getPrefix()
        if (event.isArgumentPresent(0)) {
            event.sendEmbed(getHelp(event, event.getArgument(0)))
            return
        }

        event.monke.handlers.get(PaginationHandler::class.java).addPaginator(
            Paginator(
                monke = event.monke,
                pages = getHelpPages(prefix, event),
                message = event.message
            )
        )
    }

    private fun getHelp(event: CommandEvent, command: Command): MessageEmbed {
        val prefix = event.getPrefix()
        val fields = mutableListOf(MessageEmbed.Field("**$prefix${command.name}**", getDescription(command, event, command.name), true))
        val language = event.getLanguage()
        if (command.hasChildren()) {
            fields.addAll(command.children.map {
                MessageEmbed.Field(
                        "**$prefix${it.parent.getName(language)} ${it.getName(language)}**",
                        getDescription(command, event, "${it.parent.getName(language)} ${it.getName(language)}"), true)
            })
        }
        return Embed(
            title = "${TranslationHandler.getString(event.getLanguage(), "command.help.keyword.help_for")} $prefix${command.getName(language)}",
            fields = fields
        )
    }

    private fun getDescription(command: Command, event: CommandEvent, name: String): String {
        val prefix = event.getPrefix()
        val language = event.getLanguage()

        val description = TranslationHandler.getString(language, "command.help.keyword.description")
        val usage = TranslationHandler.getString(language, "command.help.keyword.usage")

        val commandDescription = if (command is SubCommand)
                                    command.getDescription(language) // Get the child's info
                                 else command.getDescription(language) // Get the parent's info

        return "*${description}:* \n ${commandDescription}\n\n" +
                "*${usage}:* \n $prefix$name ${command.arguments.getArgumentsList(language, command)} \n\n " +
                if (command.hasArguments()) command.arguments.getArgumentsString(language, command) else ""
    }

    private fun getHelpPages(prefix: String, event: CommandEvent): List<MessageEmbed> {
        val result: MutableList<MessageEmbed> = mutableListOf()
        val commands: List<Command> = event.monke.handlers.get(CommandHandler::class.java).commandMap.values.distinct()
        val pageCount = CommandCategory.values().size
        val language = event.getLanguage()

        for (category in CommandCategory.values()) {
            val categoryCommands = commands.filter { it.category == category }
            result.add(Embed(
                title = category.getName(language),
                description = categoryCommands.joinToString(separator = "\n") { "`$prefix${it.getName(language)}` - *${it.getDescription(language)}*" },
                color = DEFAULT_EMBED_COLOUR.rgb,
                footerText = "Page ${category.ordinal + 1} / $pageCount"
            ))
        }
        return result
    }
}