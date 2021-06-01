package me.arynxd.monke.handlers

import kotlinx.coroutines.*
import me.arynxd.monke.Monke
import me.arynxd.monke.events.CommandPreprocessEvent
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandFlag
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.objects.command.SubCommand
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.objects.handlers.whenEnabled
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.util.markdownSanitize
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

const val COMMAND_PACKAGE = "me.arynxd.monke.commands"
val SUBSTITUTION_REGEX = Regex("(%[0-9]*)")

class CommandHandler(
    override val monke: Monke,
    override val dependencies: List<KClass<out Handler>> = listOf(
        TranslationHandler::class,
        GuildDataHandler::class
    )
) : Handler() {
    private val reflections = Reflections(COMMAND_PACKAGE, SubTypesScanner())
    val spaceRegex = Regex("\\s+")
    val commandMap: ConcurrentHashMap<String, Command> by whenEnabled { loadCommands() }

    fun handlePreprocessEvent(event: CommandPreprocessEvent) {
        val prefix = monke.handlers[GuildDataHandler::class].getData(event.guild.idLong).prefix

        val contentRaw = event.message.contentRaw

        val content = when {
                isBotMention(event) -> contentRaw.substring(contentRaw.indexOf(char = '>') + 1, contentRaw.length)

                contentRaw.startsWith(prefix) -> contentRaw.substring(prefix.length, contentRaw.length)

                contentRaw.startsWith(prefix.repeat(1)) -> return

                else -> return
            }
            .markdownSanitize()
            .replace(SUBSTITUTION_REGEX, "")

        val args = content.split(spaceRegex)
            .filter { it.isNotBlank() }
            .toMutableList()

        if (args.isEmpty()) {
            return
        }

        val query = args.removeAt(0).toLowerCase()
        val command = commandMap[query]

        if (command == null) {
            val message = event.message
            val language = monke.handlers[GuildDataHandler::class].getData(event.guild.idLong).language
            val thread = event.monke.handlers[CommandThreadHandler::class].getOrNew(message.idLong)
            val channel = event.channel
            val user = event.user

            val reply = CommandReply(message, channel, user, monke)
            reply.type(CommandReply.Type.EXCEPTION)
            reply.description(
                translate(
                    language = language,
                    key = "command_error.command_not_found",
                    values = arrayOf(
                        query,
                        prefix
                    )
                )
            )
            thread.post(reply)
            return
        }

        val commandEvent = CommandEvent(monke, args.toMutableList(), command, event)

        if (command.hasChildren()) {
            if (args.isEmpty()) { //Is there no additional arguments
                launchCommand(command, commandEvent)
                return
            }

            val childQuery = args[0]
            val childCommand = command.children.find { it.metaData.name.equals(childQuery, true) }

            if (childCommand == null) {
                launchCommand(command, commandEvent)
                return
            }

            args.removeAt(0)//Remove later incase the main command runs and needs this arg

            launchCommand(childCommand, CommandEvent(monke, args.toMutableList(), childCommand, event))
            return
        }

        launchCommand(command, commandEvent)
    }

    private fun isBotMention(event: CommandPreprocessEvent): Boolean {
        val content = event.message.contentRaw
        val id = event.jda.selfUser.idLong
        return content.startsWith("<@$id>") || content.startsWith("<@!$id>")
    }

    private fun launchCommand(command: Command, event: CommandEvent) {
        GlobalScope.launch {
            val isExecutable = command.isExecutable(event)

            if (!isExecutable) {
                return@launch
            }

            if (command.hasFlag(CommandFlag.SUSPENDING)) {
                try {
                    withTimeout(7_500) { //7.5 Seconds
                        command.runSuspend(event)
                    }
                }
                catch (exception: Exception) {
                    handleException(event, exception)
                }
            }
            else {
                try {
                    command.runSync(event)
                }
                catch (exception: Exception) {
                    handleException(event, exception)
                }
            }

            monke.handlers[CooldownHandler::class].addCommand(event.user, command)
            monke.handlers[MetricsHandler::class].commandCounter.labels(
                if (command is SubCommand)
                    command.parent.metaData.name
                else
                    command.metaData.name
            ).inc()
        }
    }

    private fun handleException(event: CommandEvent, exception: Exception) {
        val monke = event.monke

        val resp = event.replyAsync {
            type(CommandReply.Type.EXCEPTION)
            title("Something went wrong whilst executing that command. Please report this to the devs!")
            footer()
        }

        event.thread.post(resp)

        monke.handlers[ExceptionHandler::class]
            .handle(exception, "From command '${event.command.metaData.name}'")
    }

    /**
     * @return <code>true</code> if the command was registered, <code>false</code> if it was not.
     */
    fun registerCommand(command: Command): Boolean {
        return registerCommand(command, commandMap)
    }

    private fun registerCommand(command: Command, map: ConcurrentHashMap<String, Command>): Boolean {
        for (language in Language.getLanguages()) {
            val name = command.getName(language).toLowerCase()
            if (map.containsKey(name)) {
                return false
            }
            map[name] = command
            for (alias in command.metaData.aliases) {
                if (map.containsKey(alias)) {
                    return false
                }
                map[alias.toLowerCase()] = command
            }
        }
        return true
    }

    private fun loadCommands(): ConcurrentHashMap<String, Command> {
        val commands = ConcurrentHashMap<String, Command>()
        val classes = reflections.getSubTypesOf(Command::class.java)
        for (cls in classes) {
            val constructors = cls.constructors

            if (constructors.isEmpty() || constructors[0].parameterCount > 0) {
                continue
            }

            val instance = constructors[0].newInstance()

            if (instance is SubCommand) { //Subcommands are loaded separately
                continue
            }

            if (instance !is Command) {
                LOGGER.warn(
                    translateInternal(
                        "internal_error.non_command_class",
                        cls.simpleName
                    )
                )
                continue
            }

            if (!registerCommand(instance, commands)) {
                LOGGER.warn(
                    translateInternal(
                        "internal_error.duplicate_command",
                        cls.simpleName
                    )
                )
            }
        }
        return commands
    }
}
