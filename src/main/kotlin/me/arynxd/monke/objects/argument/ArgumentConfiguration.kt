package me.arynxd.monke.objects.argument

import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.translation.Language

class ArgumentConfiguration(val expected: List<Argument<*>>) {

    fun isConfigurationValid(): Boolean {
        if (expected.count { it.type == ArgumentType.VARARG } > 1) { // Is there more than 1 vararg
            return false
        }

        val varargIndex = expected.indexOfFirst { it.type == ArgumentType.VARARG }
        val requiredIndex = expected.indexOfLast { it.required }

        if (varargIndex != -1 && varargIndex < expected.size - 1) { // Is there a vararg not at the end of the config
            return false
        }

        if (varargIndex != -1 && expected.count { !it.required } > 0) { // Is there a vararg and an optional arg
            return false
        }

        if (requiredIndex != -1 && expected.subList(0, requiredIndex)
                .find { !it.required } != null
        ) { //Is there an optional before a required
            return false
        }


        return true
    }

    suspend fun isArgumentsValid(event: CommandEvent): ArgumentResult {
        val args = event.args.map { it.toString() }
        event.args.clear()
        val invalidArguments: MutableList<Argument<*>> = mutableListOf()
        val validArguments: MutableList<Any> = mutableListOf()
        val varargIndex = expected.indexOfLast { it.type == ArgumentType.VARARG }

        if (args.size < expected.count { it.required }) { //Missing required args
            return ArgumentResult(
                missingArguments = expected.subList(args.size, expected.size)
                    .filter { it.required }) //Collect missing args
        }

        if (args.size < varargIndex) { // Missing args before a vararg (extra checks)
            return ArgumentResult(
                missingArguments = expected.subList(args.size, expected.size)
                    .filter { it.required }) //Collect missing args
        }


        if (varargIndex == -1) {
            args.zip(expected).forEach { pair: Pair<String, Argument<*>> ->
                val result = pair.second.verify(pair.first, event)
                if (result == null) {
                    invalidArguments.add(pair.second)
                } else {
                    validArguments.add(result)
                }
            }
        }

        if (varargIndex != -1) {
            args.subList(0, varargIndex).zip(expected).forEach { pair: Pair<String, Argument<*>> ->
                val result = pair.second.verify(pair.first, event)
                if (result == null) {
                    invalidArguments.add(pair.second)
                } else {
                    validArguments.add(result)
                }
            }

            for (arg in args.subList(varargIndex, args.size)) {
                val result = expected[varargIndex].verify(arg, event)

                if (result == null) {
                    invalidArguments.add(expected[varargIndex])
                } else {
                    validArguments.add(result)
                }
            }
        }

        if (invalidArguments.isNotEmpty()) {
            return ArgumentResult(invalidArguments = invalidArguments)
        }

        event.args.addAll(validArguments)
        return ArgumentResult(validArguments = validArguments)
    }

    fun getArgumentsString(language: Language, command: Command): String {
        val req = TranslationHandler.getString(language, "keyword.required")
        val opt = TranslationHandler.getString(language, "keyword.optional")
        return expected.joinToString(separator = "\n\n", prefix = "*Arguments:*\n") {
            "<${
                it.getName(
                    language,
                    command
                )
            }> " + (if (it.required) "**($req)**" else "($opt)") + "\n ${it.getDescription(language, command)}"
        }
    }

    fun getArgumentsList(language: Language, command: Command): String {
        return expected.joinToString(separator = " ") { "<${it.getName(language, command)}>" }
    }
}