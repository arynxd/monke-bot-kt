package me.arynxd.monke.objects.command

import me.arynxd.monke.handlers.CooldownHandler
import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.util.plurify

abstract class Command(
    val metaData: CommandMetaData,
    val children: MutableList<SubCommand> = mutableListOf()
) {
    suspend fun isExecutable(event: CommandEvent): Boolean {
        val language = event.language

        if (hasFlag(CommandFlag.DISABLED) || metaData.isDisabled) {
            event.reply {
                type(CommandReply.Type.EXCEPTION)
                title(
                    translate {
                        lang = language
                        path = "command_error.disabled"
                    }
                )
                footer()
                event.thread.post(this)
            }
            return false
        }

        if (hasFlag(CommandFlag.DEVELOPER_ONLY) && !event.isDeveloper) {
            event.reply {
                type(CommandReply.Type.EXCEPTION)
                title(
                    translate {
                        lang = language
                        path = "command_error.developer_only"
                    }
                )
                footer()
                event.thread.post(this)
            }
            return false
        }

        if (!metaData.arguments.isConfigurationValid()) {
            event.reply {
                type(CommandReply.Type.EXCEPTION)
                title(
                    translate {
                        lang = language
                        path = "command_error.argument_config"
                    }
                )
                footer()
                event.thread.post(this)
            }
            return false
        }

        if (!event.member.hasPermission(event.channel, metaData.memberPermissions)) {
            val perms = metaData.memberPermissions.joinToString(separator = "\n") { it.getName() }

            event.reply {
                type(CommandReply.Type.EXCEPTION)
                title(
                    translate {
                        lang = language
                        path = "command_error.member_permission"
                        values = arrayOf(perms)
                    }
                )
                footer()
                event.thread.post(this)
            }
            return false
        }

        if (!event.selfMember.hasPermission(event.channel, metaData.botPermissions)) {
            val perms = metaData.botPermissions.joinToString(separator = "\n") { it.getName() }

            event.reply {
                type(CommandReply.Type.EXCEPTION)
                title(
                    translate {
                        lang = language
                        path = "command_error.bot_permission"
                        values = arrayOf(perms)
                    }
                )
                footer()
                event.thread.post(this)
            }
            return false
        }

        val cooldown = "%.2f".format(
            event.monke.handlers[CooldownHandler::class]
                .getRemaining(event.user, event.command) / 1000F
        )

        val isOnCooldown = event.monke.handlers[CooldownHandler::class]
            .isOnCooldown(event.user, event.command)

        if (isOnCooldown) {
            event.reply {
                type(CommandReply.Type.EXCEPTION)
                title(
                    translate {
                        lang = language
                        path = "command_error.cooldown"
                        values = arrayOf(cooldown)
                    }
                )
                footer()
                event.thread.post(this)
            }
            return false
        }

        val argResult = metaData.arguments.validateArguments(event)

        if (argResult.isMissing) {
            val requiredCount = argResult.missing.size
            val missing =
                argResult.missing.joinToString(separator = "") {
                    "*${it.getName(language, event.command)}* -- ${
                        it.getDescription(
                            language,
                            event.command
                        )
                    }\n"
                }

            event.reply {
                type(CommandReply.Type.EXCEPTION)
                description(
                    translate {
                        lang = language
                        path = "command_error.required_args"
                        values = arrayOf(
                            requiredCount,
                            requiredCount.plurify(),
                            missing
                        )
                    }
                )
                footer()
                event.thread.post(this)
            }
            return false
        }

        if (argResult.isFailure) {
            val invalidCount = argResult.failure.size
            val invalid =
                argResult.failure.joinToString(separator = "") {
                    "*${it.arg.getName(language, event.command)}* -- ${
                        translate { 
                            lang = language
                            path = it.result.error
                            values = it.result.values
                        }
                    }\n"
                }

            event.reply {
                type(CommandReply.Type.EXCEPTION)
                description(
                    translate {
                        lang = language
                        path = "command_error.invalid_args"
                        values = arrayOf(
                            invalidCount.plurify(),
                            invalid
                        )
                    }
                )
                footer()
                event.thread.post(this)
            }

            return false
        }

        if (!metaData.finalCheck(event)) {
            metaData.finalCheckFail(event)
            return false
        }
        return true
    }

    fun hasFlag(flag: CommandFlag) = metaData.flags.contains(flag)

    val hasArguments = metaData.arguments.expected.isNotEmpty()

    val hasChildren = children.isNotEmpty()

    open fun getDescription(language: Language) = translate {
        lang = language
        path = "command.${metaData.name}.description"
    }

    open fun getName(language: Language) = translate {
        lang = language
        path = "command.${metaData.name}.name"
    }

    fun getAliases(language: Language) = translate {
        lang = language
        path = "command.${metaData.name}.aliases"
    }.split("/")

    open suspend fun runSuspend(event: CommandEvent) {
        //Placeholder method
        throw UnsupportedOperationException("Incorrect run method called. Expected sync, called suspend")
    }

    open fun runSync(event: CommandEvent) {
        //Placeholder method
        throw UnsupportedOperationException("Incorrect run method called. Expected suspend, called sync")
    }
}