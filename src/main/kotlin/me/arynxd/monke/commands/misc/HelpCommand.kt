package me.arynxd.monke.commands.misc

import dev.minn.jda.ktx.Embed
import me.arynxd.monke.handlers.CommandHandler
import me.arynxd.monke.handlers.translation.translate
import me.arynxd.monke.handlers.translation.translateAll
import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.types.ArgumentCommand
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandMetaData
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.util.DEFAULT_EMBED_COLOUR
import me.arynxd.monke.util.classes.sendPaginator
import net.dv8tion.jda.api.entities.MessageEmbed


@Suppress("UNUSED")
class HelpCommand : Command(
    CommandMetaData(
        name = "help",
        description = "Shows help menu, or help for a specific command.",
        category = CommandCategory.MISC,
        aliases = listOf("?", "commands"),

        arguments = ArgumentConfiguration(
            ArgumentCommand(
                name = "command",
                description = "The command to show help for.",
                required = false,
                type = Argument.Type.REGULAR,
            )
        )
    )
) {
    override fun runSync(event: CommandEvent) {
        val prefix = event.prefix
        if (event.isArgumentPresent(0)) {
            getHelp(event, event.argument(0))
            return
        }

        event.channel.sendPaginator(event, getHelpPages(prefix, event))
    }

    private fun getHelp(event: CommandEvent, command: Command) {
        val prefix = event.prefix
        val language = event.language

        val fields = mutableListOf(
            MessageEmbed.Field(
                "**$prefix${command.getName(language)}**",
                getDescription(command, event, command.getName(language)),
                true
            )
        )

        for (child in command.children) {
            fields.add(
                MessageEmbed.Field(
                    "**$prefix${child.parent.getName(language)} ${child.getName(language)}**",
                    getDescription(child, event, "${child.parent.getName(language)} ${child.getName(language)}"),
                    true
                )
            )
        }

        event.replyAsync {
            val keywordFor = translate {
                lang = event.language
                path = "command.help.keyword.help_for"
            }
            type(CommandReply.Type.INFORMATION)
            title("$keywordFor $prefix${command.getName(language)}")
            fields(fields)
            footer()
            event.thread.post(this)
        }
    }

    private fun getDescription(command: Command, event: CommandEvent, name: String): String {
        val prefix = event.prefix
        val language = event.language

        val (description, usage) = translateAll(language) {
            part("command.help.keyword.description")
            part("command.help.keyword.usage")
        }

        val commandDescription = "__${description}:__ \n${command.getDescription(language)}"
        val args =
            "__${usage}:__ \n $prefix$name ${command.metaData.arguments.getArgumentsList(language, command)}\n\n " +
                    if (command.hasArguments)
                        command.metaData.arguments.getArgumentsString(
                            language,
                            command
                        )
                    else
                        ""

        return "\n $commandDescription\n\n $args"
    }

    private fun getHelpPages(prefix: String, event: CommandEvent): List<MessageEmbed> {
        val result = mutableListOf<MessageEmbed>()
        val commands = event.monke.handlers[CommandHandler::class]
            .commandMap
            .values
            .distinct()
            .groupBy { it.metaData.category }

        val page = translate {
            lang = event.language
            path = "command.help.keyword.page"
        }

        val pageCount = CommandCategory.values().size
        val language = event.language

        for (category in CommandCategory.values()) {
            val cat = commands[category] ?: throw IllegalStateException("Category $category was not present")
            result.add(
                Embed(
                    title = category.getName(language),
                    description = cat.joinToString(separator = "\n") {
                        "`$prefix${it.getName(language)}` - *${
                            it.getDescription(
                                language
                            )
                        }*"
                    },
                    color = DEFAULT_EMBED_COLOUR.rgb,
                    footerText = "$page ${category.ordinal + 1} / $pageCount"
                )
            )
        }
        return result
    }
}