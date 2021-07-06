package me.arynxd.monke.objects.argument

import me.arynxd.monke.handlers.translation.translate
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.SubCommand
import me.arynxd.monke.objects.translation.Language
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

abstract class Argument<T> {
    abstract val name: String
    abstract val description: String
    abstract val required: Boolean
    abstract val type: Type
    abstract val condition: (T) -> ArgumentResult<T>

    suspend fun verify(input: String, event: CommandEvent): ArgumentResult<T> {
        val conversion = convert(input, event)
        if (conversion.isError) {
            return conversion
        }

        return condition(conversion.data)
    }

    abstract suspend fun convert(input: String, event: CommandEvent): ArgumentResult<T>

    fun getDescription(language: Language, command: Command): String {
        val commandName = if (command is SubCommand) "${command.parent.metaData.name}.child.${command.metaData.name}"
        else command.metaData.name//Untranslated because name is a constant key

        return translate {
            lang = language
            path = "command.$commandName.argument.$name.description"
        }
    }

    fun getName(language: Language, command: Command): String {
        val commandName = if (command is SubCommand) "${command.parent.metaData.name}.child.${command.metaData.name}"
        else command.metaData.name //Untranslated because name is a constant key

        return translate {
            lang = language
            path = "command.$commandName.argument.$name.name"
        }
    }

    companion object {
        /**
         * Creates an empty argument instance for the purpose of trivial conversion to avoid duplicate code. Not to be used for actual argument validation.
         */
        fun <T : Argument<T>> ofEmpty(cls: KClass<T>): Argument<T> {
            val const = cls.primaryConstructor?: throw IllegalStateException("No constructor found for argument ${cls.simpleName}")
            val params = const.parameters
            val args = mapOf(
                params[0] to "name",
                params[1] to "description",
                params[2] to false,
                params[3] to Type.EMPTY,
            )
            return const.callBy(args)
        }
    }

    enum class Type {
        VARARG,
        REGULAR,
        EMPTY
    }
}

data class ArgumentResult<T>(
    private val success: T?,
    private val err: String?,
    val values: Array<Any?> = emptyArray()
) {
    val isError = err != null
    val isSuccess = success != null

    val data: T
        get() {
            if (success == null) {
                throw IllegalStateException("No data present")
            }
            return success
        }

    val error: String
        get() {
            if (err == null) {
                throw IllegalStateException("No error present")
            }
            return err
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArgumentResult<*>

        if (success != other.success) return false
        if (err != other.err) return false
        if (!values.contentEquals(other.values)) return false
        if (isError != other.isError) return false
        if (isSuccess != other.isSuccess) return false

        return true
    }

    override fun hashCode(): Int {
        var result = success?.hashCode() ?: 0
        result = 31 * result + (err?.hashCode() ?: 0)
        result = 31 * result + values.contentHashCode()
        result = 31 * result + isError.hashCode()
        result = 31 * result + isSuccess.hashCode()
        return result
    }

    companion object {
        fun <T> ofSuccess(data: T) = ArgumentResult(data, null)
        fun <T> ofFailure(err: String, vararg values: Any?) =
            ArgumentResult<T>(null, err, values.toList().toTypedArray())
    }
}



