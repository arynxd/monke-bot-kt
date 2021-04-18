package me.arynxd.monke.objects.command

import me.arynxd.monke.handlers.CooldownHandler
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.events.types.command.CommandEvent
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.util.plurifyInt

abstract class Command(
    val metaData: CommandMetaData,
    val children: MutableList<SubCommand> = mutableListOf()
) {
    suspend fun isExecutable(commandEvent: CommandEvent): Boolean {
        val language = commandEvent.getLanguage()

        if (hasFlag(CommandFlag.DISABLED) || metaData.isDisabled) {
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

        if (!metaData.arguments.isConfigurationValid()) {
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

        if (!commandEvent.member.hasPermission(commandEvent.channel, metaData.memberPermissions)) {
            val perms = metaData.memberPermissions.joinToString(separator = "\n") { it.getName() }

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

        if (!commandEvent.selfMember.hasPermission(commandEvent.channel, metaData.botPermissions)) {
            val perms = metaData.botPermissions.joinToString(separator = "\n") { it.getName() }

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

        val argResult = metaData.arguments.isArgumentsValid(commandEvent)

        if (argResult.third.isNotEmpty()) {
            val requiredCount = argResult.third.size
            val missing =
                argResult.third.joinToString(separator = "") {
                    "*${it.getName(language, commandEvent.command)}* -- ${
                        it.getDescription(
                            language,
                            commandEvent.command
                        )
                    }\n"
                }

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

        if (argResult.second.isNotEmpty()) {
            val invalidCount = argResult.second.size
            val invalid =
                argResult.second.joinToString(separator = "") {
                    "*${it.getName(language, commandEvent.command)}* -- ${
                        it.getDescription(
                            language,
                            commandEvent.command
                        )
                    }\n"
                }

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

        if (!metaData.finalCheck.invoke(commandEvent)) {
            metaData.finalCheckFail.invoke(commandEvent)
            return false
        }
        return true
    }

    fun hasFlag(flag: CommandFlag): Boolean {
        return metaData.flags.contains(flag)
    }

    fun hasArguments(): Boolean {
        return metaData.arguments.expected.isNotEmpty()
    }

    fun hasChildren(): Boolean {
        return children.isNotEmpty()
    }

    open fun getDescription(language: Language): String {
        return TranslationHandler.getString(language, "command.${metaData.name}.description")
    }

    open fun getName(language: Language): String {
        return TranslationHandler.getString(language, "command.${metaData.name}.name")
    }

    fun getAliases(language: Language): List<String> {
        return TranslationHandler.getString(language, "command.${metaData.name}.aliases").split("/")
    }

    open suspend fun runSuspend(event: CommandEvent) {
        //Placeholder method
        throw UnsupportedOperationException("Incorrect run method called")
    }

    open fun runSync(event: CommandEvent) {
        //Placeholder method
        throw UnsupportedOperationException("Incorrect run method called")
    }
}