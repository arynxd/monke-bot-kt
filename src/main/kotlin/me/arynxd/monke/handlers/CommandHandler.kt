package me.arynxd.monke.handlers

import kotlinx.coroutines.launch
import me.arynxd.monke.launch.Monke
import me.arynxd.monke.events.CommandPreprocessEvent
import me.arynxd.monke.handlers.translation.TranslationHandler
import me.arynxd.monke.handlers.translation.translate
import me.arynxd.monke.handlers.translation.translateInternal
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandFlag
import me.arynxd.monke.objects.command.SubCommand
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.objects.handlers.whenEnabled
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.util.MIN_PERMISSIONS
import me.arynxd.monke.util.equalsIgnoreCase
import me.arynxd.monke.util.hasMinimumPermissions
import me.arynxd.monke.util.markdownSanitize
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

const val COMMAND_PACKAGE = "me.arynxd.monke.commands"
val SUBSTITUTION_REGEX = Regex("(%[0-9]*)")
val SPACE_REGEX = Regex("\\s+")

class CommandHandler(
    override val monke: Monke,
    override val dependencies: List<KClass<out Handler>> = listOf(
        TranslationHandler::class,
        GuildDataHandler::class
    ),
    override val loadPredicate: () -> Boolean = { monke.config.isTesting }
) : Handler() {
    private val reflections = Reflections(COMMAND_PACKAGE, SubTypesScanner())
    val commandMap: ConcurrentHashMap<String, Command> by whenEnabled(0) { loadCommands() }

    fun handlePreprocessEvent(event: CommandPreprocessEvent) {
        val message = event.message
        val channel = event.channel
        val user = event.user
        val contentRaw = event.message.contentRaw
        val messageId = message.idLong

        val dataCache = monke.handlers[GuildDataHandler::class].getData(event.guild.idLong)
        val language = dataCache.language
        val prefix = dataCache.prefix

        val thread = event.monke.handlers[CommandThreadHandler::class].getOrNew(messageId)

        if (!event.channel.hasMinimumPermissions()) {
            message.reply(
                "Oops! I'm missing some permissions I need to function (${MIN_PERMISSIONS.joinToString { it.name }})"
            ).queue()
            return
        }


        val content = when {
            isBotMention(event) -> contentRaw.substring(contentRaw.indexOf(char = '>') + 1, contentRaw.length)

            contentRaw.startsWith(prefix) -> contentRaw.substring(prefix.length, contentRaw.length)

            contentRaw.startsWith(prefix.repeat(1)) -> return

            else -> return
        }
            .markdownSanitize()
            .replace(SUBSTITUTION_REGEX, "")

        val args = content.split(SPACE_REGEX)
            .filter { it.isNotBlank() }
            .toMutableList()

        if (args.isEmpty()) {
            return
        }

        val query = args.removeAt(0).lowercase()
        val command = commandMap[query]

        if (command == null) {
            val reply = CommandReply(messageId, channel, user, monke)
            reply.type(CommandReply.Type.EXCEPTION)
            reply.description(
                translate {
                    lang = language
                    path = "command_error.command_not_found"
                    values = arrayOf(
                        query,
                        prefix
                    )
                }
            )
            thread.post(reply)
            return
        }

        if (!command.hasFlag(CommandFlag.PAGINATED)) {
            val handler = monke.handlers[PaginationHandler::class]
            println(handler)
            handler.getById(messageId)?.stop() //Halt any existing paginators, we dont need them
            handler.remove(messageId)
            println(handler)
        }

        val commandEvent = CommandEvent(monke, args.toMutableList(), command, event)

        if (command.hasChildren) {
            if (args.isEmpty()) { //Is there no additional arguments (children to process)
                launchCommand(command, commandEvent)
                return
            }

            val childQuery = args[0]
            val childCommand = command.children.find { it.getName(language).equalsIgnoreCase(childQuery) }

            if (childCommand == null) {
                launchCommand(command, commandEvent)
                return
            }

            args.removeAt(0)//Remove later in case the main command runs and needs this arg

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

    fun launchCommand(command: Command, event: CommandEvent) {
        event.monke.coroutineScope.launch {
            val isExecutable = try {
                command.isExecutable(event)
            }
            catch (ex: Exception) {
                handleException(event, ex)
                false
            }

            if (!isExecutable) {
                return@launch
            }

            monke.handlers[CooldownHandler::class].getCache(event.guildIdLong).addCommand(event.user, command)
            monke.handlers[MetricsHandler::class].commandCounter.labels(
                if (command is SubCommand)
                    command.parent.metaData.name
                else
                    command.metaData.name
            ).inc()


            if (command.hasFlag(CommandFlag.SUSPENDING)) {
                try {
                    command.runSuspend(event)
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
        }
    }

    private fun handleException(event: CommandEvent, exception: Exception) {
        val monke = event.monke
        val errorCode =
            monke.handlers[ExceptionHandler::class].handle(exception, "From command '${event.command.metaData.name}'")
        event.replyAsync {
            type(CommandReply.Type.EXCEPTION)
            title("Something went wrong while executing the command. Please report this to the devs by giving them the following error code `$errorCode`")
            footer()
            event.thread.post(this)
        }
    }

    /**
     * @return <code>true</code> if the command was registered, <code>false</code> if it was not.
     */
    fun registerCommand(command: Command): Boolean {
        return registerCommand(command, commandMap)
    }

    private fun registerCommand(command: Command, map: ConcurrentHashMap<String, Command>): Boolean {
        for (language in Language.getLanguages()) {
            val name = command.getName(language).lowercase()
            if (map.containsKey(name)) {
                return false
            }
            map[name] = command
            for (alias in command.metaData.aliases) {
                if (map.containsKey(alias)) {
                    return false
                }
                map[alias.lowercase()] = command
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
                    translateInternal {
                        path = "internal_error.non_command_class"
                        values = arrayOf(cls.simpleName)
                    }
                )
                continue
            }

            if (!registerCommand(instance, commands)) {
                LOGGER.warn(
                    translateInternal {
                        path = "internal_error.duplicate_command"
                        values = arrayOf(cls.simpleName)
                    }
                )
            }
        }
        return commands
    }
}
