package me.arynxd.monke.objects.command

import me.arynxd.monke.handlers.CooldownHandler
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.util.plurifyInt
import net.dv8tion.jda.api.Permission

abstract class Command @JvmOverloads constructor(
    val name: String,
    val description: String,
    val category: CommandCategory,

    val children: MutableList<SubCommand> = mutableListOf(),
    val aliases: List<String> = emptyList(),
    val flags: List<CommandFlag> = emptyList(),
    val arguments: ArgumentConfiguration = ArgumentConfiguration(emptyList()),
    var isDisabled: Boolean = false,
    val cooldown: Long = 1000L,

    val finalCheck: (CommandEvent) -> Boolean = { true },
    val finalCheckFail: (CommandEvent) -> Unit = { },

    val memberPermissions: List<Permission> = emptyList(),
    val botPermissions: List<Permission> = emptyList(),
) {

    suspend fun isExecutable(commandEvent: CommandEvent): Boolean {
        val language = commandEvent.getLanguage()

        if (hasFlag(CommandFlag.DISABLED) || isDisabled) {
            commandEvent.reply {
                type(CommandReply.Type.EXCEPTION)
                title(
                    TranslationHandler.getString(
                        language = language,
                        key = "command_error.disabled"
                    )
                )
                footer()
                send()
            }
            return false
        }

        if (hasFlag(CommandFlag.DEVELOPER_ONLY) && !commandEvent.isDeveloper()) {
            commandEvent.reply {
                type(CommandReply.Type.EXCEPTION)
                title(
                    TranslationHandler.getString(
                        language = language,
                        key = "command_error.developer_only"
                    )
                )
                footer()
                send()
            }
            return false
        }

        if (!arguments.isConfigurationValid()) {
            commandEvent.reply {
                type(CommandReply.Type.EXCEPTION)
                title(
                    TranslationHandler.getString(
                        language = language,
                        key = "command_error.argument_config"
                    )
                )
                footer()
                send()
            }
            return false
        }

        if (!commandEvent.member.hasPermission(commandEvent.channel, memberPermissions)) {
            val perms = memberPermissions.joinToString(separator = "\n") { it.getName() }

            commandEvent.reply {
                type(CommandReply.Type.EXCEPTION)
                title(
                    TranslationHandler.getString(
                        language = language,
                        key = "command_error.member_permission",
                        values = arrayOf(perms)
                    )
                )
                footer()
                send()
            }
            return false
        }

        if (!commandEvent.selfMember.hasPermission(commandEvent.channel, botPermissions)) {
            val perms = botPermissions.joinToString(separator = "\n") { it.getName() }

            commandEvent.reply {
                type(CommandReply.Type.EXCEPTION)
                title(
                    TranslationHandler.getString(
                        language = language,
                        key = "command_error.bot_permission",
                        values = arrayOf(perms)
                    )
                )
                footer()
                send()
            }
            return false
        }

        val cooldown = "%.2f".format(
            commandEvent.monke.handlers.get(CooldownHandler::class)
                .getRemaining(commandEvent.user, commandEvent.command) / 1000F
        )

        val isOnCooldown = commandEvent.monke.handlers.get(CooldownHandler::class)
            .isOnCooldown(commandEvent.user, commandEvent.command)

        if (isOnCooldown) {
            commandEvent.reply {
                type(CommandReply.Type.EXCEPTION)
                title(
                    TranslationHandler.getString(
                        language = language,
                        key = "command_error.cooldown",
                        values = arrayOf(cooldown)
                    )
                )
                footer()
                send()
            }
            return false
        }

        val argResult = arguments.isArgumentsValid(commandEvent)

        if (argResult.isMissing()) {
            val requiredCount = argResult.missingArguments.size
            val missing =
                argResult.missingArguments.joinToString(separator = "") { "*${it.name}* -- ${it.description}\n" }

            commandEvent.reply {
                type(CommandReply.Type.EXCEPTION)
                description(
                    TranslationHandler.getString(
                        language = language,
                        key = "command_error.required_args",
                        values = arrayOf(
                            requiredCount,
                            plurifyInt(requiredCount),
                            missing
                        )
                    )
                )
                footer()
                send()
            }
            return false
        }

        if (argResult.isInvalid()) {
            val invalidCount = argResult.invalidArguments.size
            val invalid =
                argResult.invalidArguments.joinToString(separator = "") { "*${it.name}* -- ${it.description}\n" }

            commandEvent.reply {
                type(CommandReply.Type.EXCEPTION)
                description(
                    TranslationHandler.getString(
                        language = language,
                        key = "command_error.invalid_args",
                        values = arrayOf(
                            plurifyInt(invalidCount),
                            invalid
                        )
                    )
                )
                footer()
                send()
            }

            return false
        }

        if (!finalCheck.invoke(commandEvent)) {
            finalCheckFail.invoke(commandEvent)
            return false
        }
        return true
    }

    fun hasFlag(flag: CommandFlag): Boolean {
        return flags.contains(flag)
    }

    fun hasArguments(): Boolean {
        return arguments.expected.isNotEmpty()
    }

    fun hasChildren(): Boolean {
        return children.isNotEmpty()
    }

    open fun getDescription(language: Language): String {
        return TranslationHandler.getString(language, "command.$name.description")
    }

    open fun getName(language: Language): String {
        return TranslationHandler.getString(language, "command.$name.name")
    }

    fun getAliases(language: Language): List<String> {
        return TranslationHandler.getString(language, "command.$name.aliases").split("/")
    }

    abstract suspend fun run(event: CommandEvent)
}