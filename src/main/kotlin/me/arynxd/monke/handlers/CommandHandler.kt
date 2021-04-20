package me.arynxd.monke.handlers

import kotlinx.coroutines.*
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandFlag
import me.arynxd.monke.objects.command.CommandReply
import me.arynxd.monke.objects.command.SubCommand
import me.arynxd.monke.objects.events.interfaces.IEventListener
import me.arynxd.monke.objects.events.types.command.CommandEvent
import me.arynxd.monke.objects.events.types.command.CommandExceptionEvent
import me.arynxd.monke.objects.events.types.command.CommandPreprocessEvent
import me.arynxd.monke.objects.events.types.BaseEvent
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
) : Handler(), IEventListener {
    private val reflections = Reflections(COMMAND_PACKAGE, SubTypesScanner())
    val commandMap: ConcurrentHashMap<String, Command> by whenEnabled { loadCommands() }

    override fun onEvent(event: BaseEvent) {
        if (event is CommandPreprocessEvent) {
            handlePreprocessEvent(event)
        }
    }

    private fun handlePreprocessEvent(event: CommandPreprocessEvent) {
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
                text = translate(
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

        val commandEvent = CommandEvent(monke, args.toMutableList(), command, event)

        if (command.hasChildren()) {
            if (args.isEmpty()) { //Is there no additional arguments
                launchCommand(command, commandEvent)
                return
            }

            val childQuery = args.removeAt(0)
            val childCommand = command.children.find { it.metaData.name.equals(childQuery, true) }

            if (childCommand == null) {
                launchCommand(command, commandEvent)
                return
            }

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
        val isExecutable = runBlocking {
            command.isExecutable(event)
        }

        if (!isExecutable) {
            return
        }

        monke.handlers.get(CooldownHandler::class).addCommand(event.user, command)
        monke.handlers.get(MetricsHandler::class).commandCounter.labels(
            if (command is SubCommand)
                command.parent.metaData.name
            else
                command.metaData.name
        ).inc()

        monke.eventProcessor.fireEvent(event)

        if (command.hasFlag(CommandFlag.SUSPENDING)) {
            GlobalScope.launch {
                try {
                    withTimeout(5000) { //5 Seconds
                        command.runSuspend(event)
                    }
                }
                catch (exception: Exception) {
                    handleException(event, exception)
                }
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
    }

    private fun handleException(event: CommandEvent, exception: Exception) {
        val monke = event.monke

        event.replyAsync {
            type(CommandReply.Type.EXCEPTION)
            title("Something went wrong whilst executing that command. Please report this to the devs!")
            footer()
            send()
        }

        monke.handlers
            .get(ExceptionHandler::class)
            .handle(exception, "From command '${event.command.metaData.name}'")

        monke.eventProcessor.fireEvent(CommandExceptionEvent(monke, exception, event))
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
