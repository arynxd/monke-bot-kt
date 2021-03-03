package me.arynxd.monke.objects.argument

class ArgumentResult(
    val validArguments: List<Any> = listOf(),
    val invalidArguments: List<Argument<*>> = listOf(),
    val missingArguments: List<Argument<*>> = listOf()
) {
    fun isValid(): Boolean {
        return validArguments.isNotEmpty()
    }

    fun isInvalid(): Boolean {
        return invalidArguments.isNotEmpty()
    }

    fun isMissing(): Boolean {
        return missingArguments.isNotEmpty()
    }
}
