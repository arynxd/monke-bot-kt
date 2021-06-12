package me.arynxd.monke.objects.argument

import me.arynxd.monke.handlers.translateAll
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.translation.Language

class ArgumentConfiguration(vararg val expected: Argument<*>) {

    fun isConfigurationValid(): Boolean {
        if (expected.count { it.type == Argument.Type.VARARG } > 1) { // Is there more than 1 vararg
            return false
        }

        val varargIndex = expected.indexOfFirst { it.type == Argument.Type.VARARG }
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

    suspend fun validateArguments(event: CommandEvent): ArgumentConversion {
        val args = event.args.map { it as String }

        event.args.clear()

        val invalidArguments = mutableListOf<WrappedArgumentResult>()
        val validArguments = mutableListOf<Any>()
        val varargIndex = expected.indexOfLast { it.type == Argument.Type.VARARG }

        if (args.size < expected.count { it.required }) { //Missing required args
            return ArgumentConversion(
                emptyList(),
                emptyList(),
                expected.asList().subList(args.size, expected.size).filter { it.required } //Collect missing args
            )
        }

        if (args.size < varargIndex) { // Missing args before a vararg (extra checks)
            return ArgumentConversion(
                emptyList(),
                emptyList(),
                expected.asList().subList(args.size, expected.size).filter { it.required } //Collect missing args
            )
        }

        if (varargIndex == -1) {
            args.zip(expected).forEach {
                val result = it.second.verify(it.first, event)
                if (result.isSuccess) {
                    validArguments.add(result.data!!)
                }
                else {
                    invalidArguments.add(WrappedArgumentResult(it.second, result))
                }
            }
        }

        if (varargIndex != -1) {
            args.subList(0, varargIndex).zip(expected).forEach {
                val result = it.second.verify(it.first, event)
                if (result.isSuccess) {
                    validArguments.add(result.data!!)
                }
                else {
                    invalidArguments.add(WrappedArgumentResult(it.second, result))
                }
            }

            for (arg in args.subList(varargIndex, args.size)) {
                val result = expected[varargIndex].verify(arg, event)

                if (result.isSuccess) {
                    validArguments.add(result.data!!)
                }
                else {
                    invalidArguments.add(WrappedArgumentResult(expected[varargIndex], result))
                }
            }
        }

        if (invalidArguments.isNotEmpty()) {
            return ArgumentConversion(emptyList(), invalidArguments, emptyList())
        }

        event.args.addAll(validArguments)
        return ArgumentConversion(validArguments, emptyList(), emptyList())
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

data class WrappedArgumentResult(
    val arg: Argument<*>,
    val result: ArgumentResult<*>
)

data class ArgumentConversion(
    val success: List<Any>,
    val failure: List<WrappedArgumentResult>,
    val missing: List<Argument<*>>
) {
    val isSuccess = success.isNotEmpty()
    val isFailure = failure.isNotEmpty()
    val isMissing = missing.isNotEmpty()
}