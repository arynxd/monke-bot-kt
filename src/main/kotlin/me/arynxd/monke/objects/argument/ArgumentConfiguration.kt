package me.arynxd.monke.objects.argument

import me.arynxd.monke.handlers.translateAll
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.translation.Language

class ArgumentConfiguration(vararg val expected: Argument<*>) {

    fun isConfigurationValid(): Boolean {
        if (expected.count { it.type == Type.VARARG } > 1) { // Is there more than 1 vararg
            return false
        }

        val varargIndex = expected.indexOfFirst { it.type == Type.VARARG }
        val requiredIndex = expected.indexOfLast { it.required }

        if (varargIndex != -1 && varargIndex < expected.size - 1) { // Is there a vararg not at the end of the config
            return false
        }

        if (varargIndex != -1 && expected.count { !it.required } > 0) { // Is there a vararg and an optional arg
            return false
        }

        if (requiredIndex != -1 && expected.asList().subList(0, requiredIndex)
                .find { !it.required } != null
        ) { //Is there an optional before a required
            return false
        }

        return true
    }

    suspend fun validateArguments(event: CommandEvent): Triple<List<Any>, List<Argument<*>>, List<Argument<*>>> {
        val args =
            event.args.map { it.toString() } //Using toString to avoid un-needed casting (args should already be strings)
        event.args.clear()

        val invalidArguments = mutableListOf<Argument<*>>()
        val validArguments = mutableListOf<Any>()
        val varargIndex = expected.indexOfLast { it.type == Type.VARARG }

        if (args.size < expected.count { it.required }) { //Missing required args
            return Triple(
                emptyList(),
                emptyList(),
                expected.asList().subList(args.size, expected.size).filter { it.required } //Collect missing args
            )
        }

        if (args.size < varargIndex) { // Missing args before a vararg (extra checks)
            return Triple(
                emptyList(),
                emptyList(),
                expected.asList().subList(args.size, expected.size).filter { it.required } //Collect missing args
            )
        }

        if (varargIndex == -1) {
            args.zip(expected).forEach {
                val result = it.second.verify(it.first, event)
                if (result == null) {
                    invalidArguments.add(it.second)
                }
                else {
                    validArguments.add(result)
                }
            }
        }

        if (varargIndex != -1) {
            args.subList(0, varargIndex).zip(expected).forEach {
                val result = it.second.verify(it.first, event)
                if (result == null) {
                    invalidArguments.add(it.second)
                }
                else {
                    validArguments.add(result)
                }
            }

            for (arg in args.subList(varargIndex, args.size)) {
                val result = expected[varargIndex].verify(arg, event)

                if (result == null) {
                    invalidArguments.add(expected[varargIndex])
                }
                else {
                    validArguments.add(result)
                }
            }
        }

        if (invalidArguments.isNotEmpty()) {
            return Triple(emptyList(), invalidArguments, emptyList())
        }

        event.args.addAll(validArguments)
        return Triple(validArguments, emptyList(), emptyList())
    }

    fun getArgumentsString(language: Language, command: Command): String {
        val (req, opt) = translateAll(language) {
            part("keyword.required")
            part("keyword.optional")
        }

        return expected.joinToString(separator = "\n\n", prefix = "__Arguments:__\n") {
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