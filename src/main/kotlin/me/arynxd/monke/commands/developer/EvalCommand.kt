package me.arynxd.monke.commands.developer

import dev.minn.jda.ktx.await
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.util.postBin
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.RestAction
import okhttp3.OkHttpClient
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

@Suppress("UNUSED")
class EvalCommand : Command(
    name = "eval",
    description = "Evaluates Kotlin code.",
    category = CommandCategory.DEVELOPER,
    flags = listOf(CommandFlag.DEVELOPER_ONLY),

    arguments = ArgumentConfiguration(
        listOf(
            ArgumentString(
                name = "code",
                description = "The code to evaluate.",
                required = true,
                type = ArgumentType.VARARG,
            )
        )
    )
) {
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
        import dev.minn.jda.ktx.Embed
        import dev.minn.jda.ktx.await
        """.trimIndent()
            )

            LOGGER.info(TranslationHandler.getInternalString("internal_error.eval_reflection_warning"))
        }
    }

    private val codeBlockRegex = Regex("```[A-Za-z]*")

    override suspend fun run(event: CommandEvent) {
        engine.put("jda", event.jda)
        engine.put("api", event.jda)
        engine.put("channel", event.channel)
        engine.put("guild", event.guild)
        engine.put("member", event.member)
        engine.put("ctx", event)
        engine.put("message", event.message)
        engine.put("user", event.user)
        engine.put("bot", event.jda.selfUser)
        engine.put("monke", event.monke)

        val script = event.getVararg<String>(0)
            .joinToString(separator = " ")
            .replace(codeBlockRegex, "")

        val language = event.getLanguage()
        val client = event.monke.handlers.okHttpClient
        val startTime = System.currentTimeMillis()
        val result = doEval(script, language, client)

        val output = result.first
        val isSuccessful = result.second

        val reply = CommandReply(event)
        reply.title(
            TranslationHandler.getString(
                language = language,
                key = "command.eval.keyword.evaluated_result"
            )
        )

        reply.field(
            title = TranslationHandler.getString(language, "command.eval.keyword.duration"),
            description = "${System.currentTimeMillis() - startTime}ms",
            inline = false
        )

        reply.field(
            title = TranslationHandler.getString(language, "command.eval.keyword.code"),
            description =
            if (script.length > MessageEmbed.VALUE_MAX_LENGTH) {
                postBin(script, client) ?: "Something went wrong whilst uploading the code"
            } else {
                "```kt\n$script```"
            },
            inline = false
        )
        reply.footer()

        if (isSuccessful) {
            reply.type(CommandReply.Type.SUCCESS)
            reply.field(
                title = TranslationHandler.getString(language, "command.eval.keyword.result"),
                description = output,
                inline = false
            )

        } else {
            reply.type(CommandReply.Type.EXCEPTION)
            reply.field(
                title = TranslationHandler.getString(language, "command.eval.keyword.error"),
                description = output,
                inline = false
            )
        }
        reply.send()
    }

    private suspend fun doEval(code: String, language: Language, client: OkHttpClient): Pair<String, Boolean> {
        var successful = true
        val out =
            try {
                engine.eval(code)
            } catch (exception: Exception) {
                val st = exception.stackTraceToString()
                successful = false
                if (st.length > MessageEmbed.VALUE_MAX_LENGTH) {
                    postBin(st, client) ?: "Something went wrong whilst uploading the stacktrace"
                } else {
                    st
                }

            }

        val result = when (out) {
            null -> {
                "Null"
            }

            is RestAction<*> ->
                try {
                    out.await().toString()
                } catch (exception: ErrorResponseException) {
                    successful = false
                    val st = exception.stackTraceToString()
                    if (st.length > MessageEmbed.VALUE_MAX_LENGTH) {
                        postBin(st, client) ?: "Something went wrong whilst uploading the stacktrace"
                    } else {
                        st
                    }
                }

            else -> {
                val o = out.toString()
                if (o.isEmpty()) {
                    TranslationHandler.getString(
                        language = language,
                        key = "command.eval.keyword.no_error"
                    )
                } else {
                    if (o.length > MessageEmbed.VALUE_MAX_LENGTH) {
                        postBin(o, client) ?: "Something went wrong whilst uploading the result"
                    } else {
                        o
                    }
                }
            }
        }

        return Pair(result, successful)
    }
}
