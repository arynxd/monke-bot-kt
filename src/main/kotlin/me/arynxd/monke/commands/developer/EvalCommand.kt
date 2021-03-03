package me.arynxd.monke.commands.developer

import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandFlag
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.util.ERROR_EMBED_COLOUR
import me.arynxd.monke.util.SUCCESS_EMBED_COLOUR
import me.arynxd.monke.util.postBin
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.requests.RestAction
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
            this.eval("""
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
        import me.arynxd.monke.util.sendSuccess
        import me.arynxd.monke.util.sendError
        import me.arynxd.monke.handlers.*
        import dev.minn.jda.ktx.Embed
        """.trimIndent())

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

        val language = event.getLanguage()
        val evaluatedResult = TranslationHandler.getString(language, "command.eval.keyword.evaluated_result")

        val noError = TranslationHandler.getString(language, "command.eval.keyword.no_error")
        val status = TranslationHandler.getString(language, "command.eval.keyword.status")
        val duration = TranslationHandler.getString(language, "command.eval.keyword.duration")
        val code = TranslationHandler.getString(language, "command.eval.keyword.code")
        val error = TranslationHandler.getString(language, "command.eval.keyword.error")
        val result = TranslationHandler.getString(language, "command.eval.keyword.result")

        val builder = EmbedBuilder().setTitle(evaluatedResult)

        val script = event.getVararg<String>(0)
            .joinToString(separator = " ")
            .replace(codeBlockRegex, "")

        val startTime = System.currentTimeMillis()

        try {
            val out = engine.eval(script)

            builder.addField("$status:", "Success", true)
            builder.addField("$duration:", "${System.currentTimeMillis() - startTime}ms", true)
            builder.setColor(SUCCESS_EMBED_COLOUR)
            builder.addField("$code:", formatCodeBlock(script.let {
                if(it.length > 1000) {
                    return@let postBin(it.chunked(100).joinToString(separator = "\n"), event.monke.handlers.okHttpClient)
                } else it
            }), false)
            builder.addField("$result:", when(out) {
                is RestAction<*> -> {
                    out.queue()
                    "RestAction enqueued."
                }

                null -> noError
                else -> out
            }.toString(), true)

        } catch (exception: Exception) {
            builder.addField("$status:", "Error", true)
            builder.addField("$duration:", "${System.currentTimeMillis() - startTime}ms", true)
            builder.setColor(ERROR_EMBED_COLOUR)

            builder.addField("$code:", formatCodeBlock(script.let {
                if(it.length > 1000) {
                    return@let postBin(it.chunked(100).joinToString(separator = "\n"), event.monke.handlers.okHttpClient)
                } else it
            }), false)

            builder.addField("$error:", formatCodeBlock(exception.toString().let {
                if(it.length > 1000) {
                    return@let postBin(it.chunked(100).joinToString(separator = "\n"), event.monke.handlers.okHttpClient)
                } else it
            }), true)
        }

        event.sendEmbed(builder.build())
    }

    private fun formatCodeBlock(stringOrUrl: String?): String = //Omit the code-block if we posted a haste link
        stringOrUrl?.let {
            if(it.startsWith("https://"))
                it
            else "```kt\n$it```"
    } ?: "null"
}
