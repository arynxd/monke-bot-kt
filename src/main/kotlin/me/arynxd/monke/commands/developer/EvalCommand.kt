package me.arynxd.monke.commands.developer

import dev.minn.jda.ktx.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import me.arynxd.monke.handlers.translate
import me.arynxd.monke.handlers.translateInternal
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.events.types.command.CommandEvent
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.util.postBin
import me.arynxd.monke.util.takeOrHaste
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.RestAction
import okhttp3.OkHttpClient
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager


@Suppress("UNUSED")
class EvalCommand : Command(
    CommandMetaData(
        name = "eval",
        description = "Evaluates Kotlin code.",
        category = CommandCategory.DEVELOPER,
        flags = listOf(CommandFlag.DEVELOPER_ONLY, CommandFlag.SUSPENDING),

        arguments = ArgumentConfiguration(
            listOf(
                ArgumentString(
                    name = "code",
                    description = "The code to evaluate.",
                    required = true,
                    type = Type.VARARG,
                )
            )
        )
    )
) {
    private val saveFunc = """
        fun Any.save() {
            output.data.add(this)
        }
        """.trimIndent()

    private val engine: ScriptEngine by lazy {
        ScriptEngineManager().getEngineByExtension("kts")!!.apply {
            this.eval(
                    """
                import net.dv8tion.jda.api.*
                import net.dv8tion.jda.api.entities.*
                import net.dv8tion.jda.api.exceptions.*
                import net.dv8tion.jda.api.utils.*
                import net.dv8tion.jda.api.requests.restaction.*
                import net.dv8tion.jda.api.requests.*
                import kotlin.collections.*
                import kotlinx.coroutines.*
                import java.util.*
                import java.util.concurrent.*
                import java.util.stream.*
                import java.io.*
                import java.time.* 
                import me.arynxd.monke.handlers.*
                import me.arynxd.monke.objects.command.*
                import me.arynxd.monke.objects.command.CommandReply.Type
                import dev.minn.jda.ktx.Embed
                import dev.minn.jda.ktx.await
                """.trimIndent()
            )

            LOGGER.info(translateInternal("internal_error.eval_reflection_warning"))
        }
    }

    private val codeBlockRegex = Regex("```[A-Za-z]*")

    override suspend fun runSuspend(event: CommandEvent) {
        val outputObj = EvalOutput(mutableListOf())
        val monke = event.monke

        val oldConsole = System.out
        val newOutStream = ByteArrayOutputStream()
        val newSysOut = PrintStream(newOutStream)

        engine.put("output", outputObj)
        engine.put("jda", event.jda)
        engine.put("api", event.jda)
        engine.put("channel", event.channel)
        engine.put("guild", event.guild)
        engine.put("member", event.member)
        engine.put("ctx", event)
        engine.put("message", event.message)
        engine.put("user", event.user)
        engine.put("bot", event.jda.selfUser)
        engine.put("monke", monke)

        val script = event.vararg<String>(0)
            .joinToString(separator = " ")
            .replace(codeBlockRegex, "")

        val language = event.language()
        val okHttpClient = monke.handlers.okHttpClient
        val startTime = System.currentTimeMillis()

        System.setOut(newSysOut)

        val result = doEval(saveFunc + script, language, okHttpClient)

        val sysOut = String(newOutStream.toByteArray()).takeOrHaste(100, monke).let {
            if (it.isBlank()) {
                return@let "Nothing printed"
            }
            else {
                return@let it
            }
        }

        System.setOut(oldConsole)

        val outputArr = outputObj.data.joinToString(separator = ", ").takeOrHaste(100, monke).let {
            if (it.isBlank()) {
                return@let "Nothing saved"
            }
            else {
                return@let it
            }
        }

        withContext(Dispatchers.IO) {
            newOutStream.close()
            newSysOut.close()
        }

        val output = result.first
        val isSuccessful = result.second

        val reply = CommandReply(event)
        reply.title(
            translate(
                language = language,
                key = "command.eval.keyword.evaluated_result"
            )
        )

        reply.field(
            title = translate(language, "command.eval.keyword.code"),
            description = "```kt\n${script.takeOrHaste(MessageEmbed.VALUE_MAX_LENGTH, monke)}```",
            inline = true
        )

        reply.field(
            title = "Language",
            description = "Kotlin",
            inline = true
        )

        reply.field(
            title = translate(language, "command.eval.keyword.duration"),
            description = "${System.currentTimeMillis() - startTime}ms",
            inline = true
        )

        if (isSuccessful) {
            reply.type(CommandReply.Type.SUCCESS)
            reply.field(
                title = translate(language, "command.eval.keyword.result"),
                description = output,
                inline = true
            )

        }
        else {
            reply.type(CommandReply.Type.EXCEPTION)
            reply.field(
                title = translate(language, "command.eval.keyword.error"),
                description = output,
                inline = false
            )
        }

        reply.field(
            title = "Saved Output",
            description = outputArr,
            inline = true
        )

        reply.field(
            title = "Saved Console Output",
            description = sysOut,
            inline = true
        )
        reply.footer()
        reply.send()
    }

    private suspend fun doEval(code: String, language: Language, client: OkHttpClient): Pair<String, Boolean> {
        var successful = true
        val out =
            try {
                withTimeout(5_000) {
                    return@withTimeout engine.eval(code)
                }
            }
            catch (exception: Exception) {
                val st = exception.stackTraceToString()
                successful = false
                if (st.length > MessageEmbed.VALUE_MAX_LENGTH) {
                    postBin(st, client) ?: "Something went wrong whilst uploading the stacktrace"
                }
                else {
                    st
                }
            }

        val result = when (out) {
            null -> {
                "null"
            }
            is RestAction<*> ->
                try {
                    out.await()?.toString() ?: "null"
                }
                catch (exception: ErrorResponseException) {
                    successful = false
                    val st = exception.stackTraceToString()
                    if (st.length > MessageEmbed.VALUE_MAX_LENGTH) {
                        postBin(st, client) ?: "Something went wrong whilst uploading the stacktrace"
                    }
                    else {
                        st
                    }
                }

            else -> {
                val o = out.toString()
                if (o.isBlank()) {
                    translate(
                        language = language,
                        key = "command.eval.keyword.no_error"
                    )
                }
                else {
                    if (o.length > MessageEmbed.VALUE_MAX_LENGTH) {
                        postBin(o, client) ?: "Something went wrong whilst uploading the result"
                    }
                    else {
                        o
                    }
                }
            }
        }

        return Pair(result, successful)
    }

    //Wrapper class for a data list because script engine can't handle a regular array????
    data class EvalOutput(
        val data: MutableList<Any>
    )
}
