package me.arynxd.monke.handlers

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.objects.handlers.whenEnabled
import net.dv8tion.jda.api.entities.Message
import java.util.*
import kotlin.reflect.KClass

@Suppress("UNUSED")
class ExceptionHandler(
    override val monke: Monke,
    override val dependencies: List<KClass<out Handler>> = listOf(ConfigHandler::class)
) : Handler() {
    private val webhookUrl: String by whenEnabled(1) { monke.handlers[ConfigHandler::class].config.logWebhook }
    private val webhookClient: WebhookClient by whenEnabled(2) { WebhookClient.withUrl(webhookUrl) }
    private val avatarUrl: String by whenEnabled(3) { monke.jda.selfUser.effectiveAvatarUrl }

    private val monkeClassName = monke.javaClass.packageName

    fun handle(throwable: Throwable, information: String = "null"): String {
        val code = generateErrorCode()
        LOGGER.error("An uncaught exception has occurred: Error Code ($code) ($information)", throwable)

        val content = generateContent(throwable, code)
            .take(Message.MAX_CONTENT_LENGTH)
            .substringBeforeLast("\n")

        val message = WebhookMessageBuilder()
            .setAvatarUrl(avatarUrl)
            .setContent(content)
            .build()

        webhookClient.send(message)
        return code
    }

    private fun generateContent(st: Throwable, errorCode: String): String {
        val sb = StringBuilder()

        sb.append(st.toString()).append("\n")
        for (elem in st.stackTrace) { //Basically printStackTrace but with our classes in bold

            val head =
                if (elem.className.startsWith(monkeClassName)) {
                    " --> ${elem.className}.${elem.methodName}"

                }
                else {
                    "${elem.className}.${elem.methodName}"
                }

            val body = "\t @ $head(" + when {
                elem.isNativeMethod -> "Native Method"
                elem.lineNumber >= 0 -> "${elem.fileName}: ${elem.lineNumber}"
                else -> "Unknown Source"
            } + ")"

            sb.append(body).append("\n")
        }

        val title = "An uncaught exception has occurred. Error Code (**$errorCode**)"
        val thread = Thread.currentThread()

        val threadInfo = "__THREAD-INFO__\n\n" +
                "ID: ${thread.id}\n" +
                "Name: ${thread.name}\n" +
                "Interrupted: ${thread.isInterrupted}\n" +
                "Alive: ${thread.isAlive}"

        return "$title\n\n$threadInfo\n\n__STACKTRACE__:\n$sb"
    }

    private fun generateErrorCode() = UUID.randomUUID().toString().substringBefore("-") //Random set of chars

    override fun onDisable() {
        webhookClient.close()
    }
}
