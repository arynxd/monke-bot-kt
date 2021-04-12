package me.arynxd.monke.handlers

import io.github.classgraph.ClassGraph
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import me.arynxd.monke.Monke
import me.arynxd.monke.events.GuildMessageEvent
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandReply
import me.arynxd.monke.objects.command.SubCommand
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.objects.handlers.whenEnabled
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.util.markdownSanitize
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

const val COMMAND_PACKAGE = "me.arynxd.monke.commands"
val SUBSTITUTION_REGEX = Regex("(%[0-9]*)")

class CommandHandler @JvmOverloads constructor(
    override val monke: Monke,
    override val dependencies: List<KClass<out Handler>> = listOf(
        TranslationHandler::class,
        GuildDataHandler::class
    )
) : Handler() {
    private val classGraph = ClassGraph().acceptPackages(COMMAND_PACKAGE)
    val commandMap: ConcurrentHashMap<String, Command> by whenEnabled { loadCommands() }

    fun handle(event: GuildMessageEvent) {
        val prefix = monke.handlers.get(GuildDataHandler::class).getData(event.guild.idLong).prefix

        val contentRaw = event.message.contentRaw

        val content = markdownSanitize(
            when {
                isBotMention(event) -> contentRaw.substring(contentRaw.indexOf(char = '>') + 1, contentRaw.length)

                contentRaw.startsWith(prefix) -> contentRaw.substring(prefix.length, contentRaw.length)

                contentRaw.startsWith(prefix.repeat(1)) -> return

                else -> return
            }
        ).replace(SUBSTITUTION_REGEX, "")

        val args = content.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .toMutableList()

        if (args.isEmpty()) {
            return
        }

        val query = args.removeAt(0).toLowerCase()
        val command = commandMap[query]

        if (command == null) {
            val language = monke.handlers.get(GuildDataHandler::class).getData(event.guild.idLong).language
            CommandReply.sendError(
                message = event.message,
                text = TranslationHandler.getString(
                    language = language,
                    key = "command_error.command_not_found",
                    values = arrayOf(
                        query,
                        prefix
                    )
                )
            )
            return
        }

        val commandEvent = CommandEvent(event, command, args.toMutableList(), monke)

        if (command.hasChildren()) {
            if (args.isEmpty()) { //Is there no additional arguments
                launchCommand(command, commandEvent)
                return
            }

            val childQuery = args[0]
            val childCommand = command.children.find { it.name.equals(childQuery, true) }

            if (childCommand == null) {
                launchCommand(command, commandEvent)
                return
            }

            args.removeAt(0)
            launchCommand(childCommand, CommandEvent(event, childCommand, args.toMutableList(), monke))
            return
        }

        launchCommand(command, commandEvent)
    }

    private fun isBotMention(event: GuildMessageEvent): Boolean {
        val content = event.message.contentRaw
        val id = event.jda.selfUser.idLong
        return content.startsWith("<@$id>") || content.startsWith("<@!$id>")
    }

    private fun launchCommand(command: Command, event: CommandEvent) {
        GlobalScope.launch {
            if (!command.isExecutable(event)) {
                return@launch
            }

            monke.handlers.get(CooldownHandler::class).addCommand(event.user, command)
            monke.handlers.get(MetricsHandler::class).commandCounter.labels(
                if (command is SubCommand)
                    command.parent.name
                else
                    command.name
            ).inc()

            try {
                withTimeout(5000) { //5 Seconds
                    command.run(event)
                }
            }
            catch (exception: Exception) {
                event.reply {
                    type(CommandReply.Type.EXCEPTION)
                    title("Something went wrong whilst executing that command. Please report this to the devs!")
                    footer()
                    send()
                }

                event.monke.handlers
                    .get(ExceptionHandler::class)
                    .handle(exception, "From command '${event.command.name}'")
            }
        }
    }

    fun registerCommand(command: Command): Boolean {
        return registerCommand(command, commandMap)
    }

    fun registerCommand(command: Command, map: ConcurrentHashMap<String, Command>): Boolean {
        for (language in Language.getLanguages()) {
            val name = command.getName(language).toLowerCase()
            if (map.containsKey(name)) {
                return false
            }
            map[name] = command
            for (alias in command.aliases) {
                if (map.containsKey(alias)) {
                    return false
                }
                map[alias.toLowerCase()] = command
            }
        }
        return true
    }

    private fun loadCommands(): ConcurrentHashMap<String, Command> {
        val commands = ConcurrentHashMap<String, Command> ()
        classGraph.scan().use {
            for (cls in it.allClasses) {
                val constructors = cls.loadClass().declaredConstructors

                if (constructors.isEmpty() || constructors[0].parameterCount > 0) {
                    continue
                }

                val instance = constructors[0].newInstance()

                if (instance is SubCommand) {
                    continue
                }

                if (instance !is Command) {
                    LOGGER.warn(
                        TranslationHandler.getInternalString(
                            "internal_error.non_command_class",
                            cls.simpleName
                        )
                    )
                    continue
                }

                if (!registerCommand(instance, commands)) {
                    LOGGER.warn(
                        TranslationHandler.getInternalString(
                            "internal_error.duplicate_command",
                            cls.simpleName
                        )
                    )
                }
            }
        }
        return commands
    }
}
