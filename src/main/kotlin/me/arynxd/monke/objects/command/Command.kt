package me.arynxd.monke.objects.command

import me.arynxd.monke.handlers.CooldownHandler
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.util.plurifyInt
import me.arynxd.monke.util.sendError
import net.dv8tion.jda.api.Permission

abstract class Command(
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
    val finalCheckFail: (CommandEvent) -> Unit = {},

    val memberPermissions: List<Permission> = emptyList(),
    val botPermissions: List<Permission> = emptyList(),
) {

    suspend fun isExecutable(commandEvent: CommandEvent): Boolean {

        if (hasFlag(CommandFlag.DISABLED) || isDisabled) {
            sendError(commandEvent.message, TranslationHandler.getString(Language.EN_US, "command_error.disabled"))
            return false
        }

        if (hasFlag(CommandFlag.DEVELOPER_ONLY) && !commandEvent.isDeveloper()) {
            sendError(
                commandEvent.message,
                TranslationHandler.getString(Language.EN_US, "command_error.developer_only")
            )
            return false
        }

        if (!arguments.isConfigurationValid()) {
            sendError(
                commandEvent.message,
                TranslationHandler.getString(Language.EN_US, "command_error.argument_config")
            )
            return false
        }

        if (!commandEvent.member.hasPermission(commandEvent.channel, memberPermissions)) {
            val perms = memberPermissions.joinToString(separator = "\n") { it.getName() }
            sendError(
                commandEvent.message,
                TranslationHandler.getString(Language.EN_US, "command_error.member_permission", perms)
            )
            return false
        }

        if (!commandEvent.selfMember.hasPermission(commandEvent.channel, botPermissions)) {
            val perms = botPermissions.joinToString(separator = "\n") { it.getName() }
            sendError(
                commandEvent.message,
                TranslationHandler.getString(Language.EN_US, "command_error.bot_permission", perms)
            )
            return false
        }

        val cooldown = "%.2f".format(
            commandEvent.monke.handlers.get(CooldownHandler::class)
                .getRemaining(commandEvent.user, commandEvent.command) / 1000F
        )

        val isOnCooldown = commandEvent.monke.handlers.get(CooldownHandler::class)
            .isOnCooldown(commandEvent.user, commandEvent.command)

        if (isOnCooldown) {
            sendError(
                commandEvent.message,
                TranslationHandler.getString(Language.EN_US, "command_error.cooldown", cooldown)
            )
            return false
        }

        val argResult = arguments.isArgumentsValid(commandEvent)

        if (argResult.isMissing()) {
            val requiredCount = argResult.missingArguments.size

            sendError(commandEvent.message,
                TranslationHandler.getString(Language.EN_US, "command_error.required_args",
                    requiredCount,
                    plurifyInt(requiredCount),
                    argResult.missingArguments.joinToString { "*${it.name}* -- ${it.description}\n" }
                )
            )
            return false
        }

        if (argResult.isInvalid()) {
            val invalidCount = argResult.invalidArguments.size

            sendError(commandEvent.message,
                TranslationHandler.getString(Language.EN_US, "command_error.invalid_args",
                    plurifyInt(invalidCount),
                    argResult.invalidArguments.joinToString { "*${it.name}* -- ${it.description}\n" }
                )
            )

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