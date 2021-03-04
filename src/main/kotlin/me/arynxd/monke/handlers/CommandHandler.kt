package me.arynxd.monke.handlers

import io.github.classgraph.ClassGraph
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.arynxd.monke.Monke
import me.arynxd.monke.events.GuildMessageEvent
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.SubCommand
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.util.markdownSanitize
import me.arynxd.monke.util.sendError
import org.slf4j.LoggerFactory
import java.lang.reflect.Constructor
import java.util.*
import kotlin.collections.LinkedHashMap

const val COMMAND_PACKAGE = "me.arynxd.monke.commands"
val SUBSTITUTION_REGEX = Regex("(%[0-9]*)")

class CommandHandler @JvmOverloads constructor(
    override val monke: Monke,
    override val dependencies: List<Class<out Handler>> = listOf(
        TranslationHandler::class.java,
        GuildSettingsHandler::class.java
    )
) : Handler() {
    private val logger = LoggerFactory.getLogger(CommandHandler::class.java)
    private val classGraph: ClassGraph = ClassGraph().acceptPackages(COMMAND_PACKAGE)
    val commandMap: LinkedHashMap<String, Command> by lazy { loadCommands() }

    fun handle(event: GuildMessageEvent) {
        val prefix = monke.handlers.get(GuildSettingsHandler::class.java).getCache(event.guild.idLong).prefix

        val contentRaw = event.message.contentRaw

        val content: String = markdownSanitize(
                when {
                isBotMention(event) -> contentRaw.substring(contentRaw.indexOf(char = '>') + 1, contentRaw.length)

                contentRaw.startsWith(prefix) -> contentRaw.substring(prefix.length, contentRaw.length)

                contentRaw.startsWith(prefix.repeat(1)) -> return

                else -> return
            }
        ).replace(SUBSTITUTION_REGEX, "")

        val args: MutableList<String> = content.split(Regex("\\s+")).toMutableList()
        args.removeIf { it.isBlank() }

        if (args.isEmpty()) {
            return
        }

        val query: String = args.removeAt(0).toLowerCase()
        val command: Command? = commandMap[query]

        if (command == null) {
            sendError(event.message, TranslationHandler.getString(Language.EN_US, "command_error.command_not_found", query, prefix))
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
        val content: String = event.message.contentRaw
        val id: Long = event.jda.selfUser.idLong
        return content.startsWith("<@$id>") || content.startsWith("<@!$id>")
    }

    private fun launchCommand(command: Command, event: CommandEvent) {
        GlobalScope.launch {
            if (!command.isExecutable(event)) {
                return@launch
            }
            monke.handlers.get(CooldownHandler::class.java).addCommand(event.user, command)
            monke.handlers.get(MetricsHandler::class.java).commandCounter.labels(
                    if (command is SubCommand)
                        command.parent.name
                    else
                        command.name).inc()
            command.run(event)
        }
    }

    private fun loadCommands(): LinkedHashMap<String, Command> {
        val commands: LinkedHashMap<String, Command> = LinkedHashMap()
        classGraph.scan().use { result ->
            for (cls in result.allClasses) {
                val constructors: Array<Constructor<*>> = cls.loadClass().declaredConstructors

                if (constructors.isEmpty() || constructors[0].parameterCount > 0) {
                    continue
                }

                val instance: Any = constructors[0].newInstance()
                if (instance is SubCommand) {
                    continue
                }

                if (instance !is Command) {
                    logger.warn(TranslationHandler.getInternalString("internal_error.non_command_class", cls.simpleName))
                    continue
                }

                if (commands.containsKey(instance.name)) {
                    logger.warn(TranslationHandler.getInternalString("internal_error.duplicate_command", cls.simpleName))
                    continue
                }

                for (language in Language.getLanguages()) {
                    commands[instance.getName(language).toLowerCase()] = instance
                    for (alias in instance.aliases) {
                        commands[alias.toLowerCase()] = instance
                    }
                }
            }
        }
        return commands
    }
}
