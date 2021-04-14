package me.arynxd.monke.commands.misc

import dev.minn.jda.ktx.Embed
import me.arynxd.monke.handlers.CommandHandler
import me.arynxd.monke.handlers.PaginationHandler
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.Paginator
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentCommand
import me.arynxd.monke.objects.command.*
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
    override fun runSync(event: CommandEvent) {
        val prefix = event.getPrefix()
        if (event.isArgumentPresent(0)) {
            getHelp(event, event.getArgument(0))
            return
        }

        event.monke.handlers.get(PaginationHandler::class).addPaginator(
            Paginator(
                monke = event.monke,
                pages = getHelpPages(prefix, event),
                message = event.message
            )
        )
    }

    private fun getHelp(event: CommandEvent, command: Command) {
        val prefix = event.getPrefix()
        val fields = mutableListOf(
            MessageEmbed.Field(
                "**$prefix${command.name}**",
                getDescription(command, event, command.name),
                true
            )
        )
        val language = event.getLanguage()
        if (command.hasChildren()) {
            for (child in command.children) {
                fields.add(
                    MessageEmbed.Field(
                        "**$prefix${child.parent.getName(language)} ${child.getName(language)}**",
                        getDescription(child, event, "${child.parent.getName(language)} ${child.getName(language)}"),
                        true
                    )
                )
            }
        }

        event.replyAsync {
            val keywordFor = TranslationHandler.getString(
                language = event.getLanguage(),
                key = "command.help.keyword.help_for"
            )
            type(CommandReply.Type.INFORMATION)
            title("$keywordFor $prefix${command.getName(language)}")
            fields(fields)
            footer()
            send()
        }
    }

    private fun getDescription(command: Command, event: CommandEvent, name: String): String {
        val prefix = event.getPrefix()
        val language = event.getLanguage()

        val description = TranslationHandler.getString(language, "command.help.keyword.description")
        val usage = TranslationHandler.getString(language, "command.help.keyword.usage")

        val commandDescription =
            if (command is SubCommand)
                "*${description}:* ${command.getDescription(language)}" // Get the child's info
            else
                "*${description}:* ${command.getDescription(language)}" // Get the parent's info

        val args =
            if (command is SubCommand) {
                "*${usage}:* \n $prefix$name ${command.arguments.getArgumentsList(language, command)} \n\n " +
                        if (command.hasArguments()) command.arguments.getArgumentsString(language, command) else ""
            }
            else {
                "*${usage}:* \n $prefix$name ${command.arguments.getArgumentsList(language, command)} \n\n " +
                        if (command.hasArguments()) command.arguments.getArgumentsString(language, command) else ""
            }

        return "\n $commandDescription\n\n $args"
    }

    private fun getHelpPages(prefix: String, event: CommandEvent): List<MessageEmbed> {
        val result = mutableListOf<MessageEmbed>()
        val commands =
            event.monke.handlers.get(CommandHandler::class).commandMap.values.distinct().groupBy { it.category }
        val pageCount = CommandCategory.values().size
        val language = event.getLanguage()

        for (category in CommandCategory.values()) {
            val categoryCommands =
                commands[category] ?: throw IllegalStateException("Category $category was not present")
            result.add(
                Embed(
                    title = category.getName(language),
                    description = categoryCommands.joinToString(separator = "\n") {
                        "`$prefix${it.getName(language)}` - *${
                            it.getDescription(
                                language
                            )
                        }*"
                    },
                    color = DEFAULT_EMBED_COLOUR.rgb,
                    footerText = "Page ${category.ordinal + 1} / $pageCount"
                )
            )
        }
        return result
    }
}