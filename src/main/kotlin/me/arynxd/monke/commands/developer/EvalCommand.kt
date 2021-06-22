package me.arynxd.monke.commands.developer

import dev.minn.jda.ktx.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import me.arynxd.monke.handlers.TaskHandler
import me.arynxd.monke.handlers.translation.translate
import me.arynxd.monke.handlers.translation.translateInternal
import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.util.takeOrHaste
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.RestAction
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.concurrent.TimeUnit
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.script.experimental.jsr223.KotlinJsr223DefaultScriptEngineFactory


@Suppress("UNUSED")
class EvalCommand : Command(
    CommandMetaData(
        name = "eval",
        description = "Evaluates Kotlin code.",
        category = CommandCategory.DEVELOPER,
        flags = listOf(CommandFlag.DEVELOPER_ONLY, CommandFlag.SUSPENDING),

        arguments = ArgumentConfiguration(
            ArgumentString(
                name = "code",
                description = "The code to evaluate.",
                required = true,
                type = Argument.Type.VARARG
            )
        )
    )
) {

    private val engine: ScriptEngine by lazy {
        ScriptEngineManager().getEngineByExtension("kts")!!.apply {
            this.eval(
                """
                import net.dv8tion.jda.api.*;
                import net.dv8tion.jda.api.entities.*;
                import net.dv8tion.jda.api.exceptions.*;
                import net.dv8tion.jda.api.utils.*;
                import net.dv8tion.jda.api.requests.restaction.*;
                import net.dv8tion.jda.api.requests.*;
                
                import kotlin.collections.*;
                import kotlinx.coroutines.*;
                
                import java.util.*;
                import java.util.concurrent.*;
                import java.util.stream.*;
                import java.io.*;
                import java.time.* ;
                
                import me.arynxd.monke.handlers.*;

                import me.arynxd.monke.objects.command.threads.CommandReply;
                
                import dev.minn.jda.ktx.Embed;
                import dev.minn.jda.ktx.await;
                """.trimIndent()
            )

            LOGGER.info(translateInternal { path = "internal_error.eval_reflection_warning" })
        }
    }

    private val codeBlockRegex = Regex("```[A-Za-z]*")

    override suspend fun runSuspend(event: CommandEvent) {
        val monke = event.monke

        val oldConsole = System.out
        val newOutStream = ByteArrayOutputStream()
        val newSysOut = PrintStream(newOutStream)

        val script = event.vararg<String>(0)
            .joinToString(separator = " ")
            .replace(codeBlockRegex, "")

        val appendedScript = """
            fun Any?.save() {
                output.data.add(this)
                output.saveHook?.invoke(this)?: throw IllegalStateException("Save hook was null, this should never happen")
            }
            
            runBlocking {
                $script;
            }
        """.trimIndent()


        val language = event.language

        System.setOut(newSysOut)
        val outputObj = EvalOutput(mutableListOf(), null)
        var reply = buildReply(event, script, true, "---", "---", "---")

        val restActionHandler: (Throwable) -> Unit = {
            event.monke.handlers[TaskHandler::class].addOneShot(2, TimeUnit.SECONDS) {
                val value = it.message.toString()
                reply.type(CommandReply.Type.EXCEPTION)
                reply.setField(0, translate {
                    lang = language
                    path = "command.eval.keyword.result"
                }, value, true)
                event.thread.post(reply)
            }
        }

        val saveHandler: (Any?) -> Unit = {
            val outputArr = outputObj.data.joinToString(separator = ", ")
            val title = translate {
                lang = language
                path = "command.eval.keyword.saved_output"
            }

            reply.setField(1, title, outputArr, true)
            event.thread.post(reply)
        }

        outputObj.saveHook = saveHandler

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
        engine.put("language", language)

        event.thread.post(reply)

        val defaultFailure = RestAction.getDefaultFailure()
        RestAction.setDefaultFailure(restActionHandler)

        val result = doEval(appendedScript, event)

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

        val output = result.first
        val isSuccessful = result.second

        reply = buildReply(
            event = event,
            script = script,
            success = isSuccessful,
            output = output,
            saved = outputArr,
            sysOut = sysOut
        )

        withContext(Dispatchers.IO) {
            newOutStream.close()
            newSysOut.close()
        }

        event.thread.post(reply)
        event.monke.handlers[TaskHandler::class].addOneShot(2500, TimeUnit.MILLISECONDS) {
            RestAction.setDefaultFailure(defaultFailure)
        }
    }

    private suspend fun buildReply(
        event: CommandEvent,
        script: String,
        success: Boolean,
        output: String,
        sysOut: String,
        saved: String
    ) = event.reply {
        val language = event.language
        title(
            translate {
                lang = language
                path = "command.eval.keyword.evaluated_result"
            }
        )
        if (success) {
            type(CommandReply.Type.SUCCESS)
            field(
                title = translate {
                    lang = language
                    path = "command.eval.keyword.result"
                },
                description = output,
                inline = true
            )

        }
        else {
            type(CommandReply.Type.EXCEPTION)
            field(
                title = translate {
                    lang = language
                    path = "command.eval.keyword.error"
                },
                description = output,
                inline = true
            )
        }

        field(
            title = translate {
                lang = language
                path = "command.eval.keyword.saved_output"
            },
            description = saved,
            inline = true
        )

        field(
            title = translate {
                lang = language
                path = "command.eval.keyword.saved_stdout"
            },
            description = sysOut,
            inline = true
        )
        field(
            title = translate {
                lang = language
                path = "command.eval.keyword.code"
            },
            description = "```kt\n${script.takeOrHaste(MessageEmbed.VALUE_MAX_LENGTH, monke)}```",
            inline = false
        )
        footer()
    }

    private suspend fun doEval(code: String, event: CommandEvent): Pair<String, Boolean> {
        val language = event.language

        var successful = true
        val out =
            try {
                withTimeout(5_000) {
                    return@withTimeout withContext(Dispatchers.IO) {
                        engine.eval(code)
                    }
                }
            }
            catch (exception: Exception) {
                successful = false
                val haste = exception
                    .stackTraceToString()
                    .takeOrHaste(MessageEmbed.VALUE_MAX_LENGTH, event.monke)
                "${exception.message}\n$haste"
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
                    val haste = exception
                        .stackTraceToString()
                        .takeOrHaste(MessageEmbed.VALUE_MAX_LENGTH, event.monke)
                    "${exception.message}\n$haste"
                }

            else -> {
                val o = out.toString()
                if (o.isBlank()) {
                    translate {
                        lang = language
                        path = "command.eval.keyword.no_error"
                    }
                }
                else {
                    o.takeOrHaste(MessageEmbed.VALUE_MAX_LENGTH, event.monke)
                }
            }
        }

        return Pair(result, successful)
    }

    //Wrapper class for a data list because script engine can't handle a regular array or functional variables????
    data class EvalOutput(
        val data: MutableList<Any?>,
        var saveHook: ((Any?) -> Unit)? // dont worry this is fine :)
    )
}
