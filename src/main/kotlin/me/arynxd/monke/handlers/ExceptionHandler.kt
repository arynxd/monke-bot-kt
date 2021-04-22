package me.arynxd.monke.handlers

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import dev.minn.jda.ktx.Embed
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.objects.handlers.whenEnabled
import me.arynxd.monke.util.ERROR_EMBED_COLOUR
import net.dv8tion.jda.api.entities.MessageEmbed
import java.time.Instant
import kotlin.reflect.KClass

@Suppress("UNUSED")
class ExceptionHandler(
    override val monke: Monke,
    override val dependencies: List<KClass<out Handler>> = listOf(ConfigHandler::class)
) : Handler() {
    private val webhookUrl: String by whenEnabled { monke.handlers.get(ConfigHandler::class).config.logWebhook }
    private val avatarUrl: String by whenEnabled { monke.jda.selfUser.effectiveAvatarUrl }
    private val webhookClient: WebhookClient by lazy { WebhookClient.withUrl(webhookUrl) }

    fun handle(throwable: Throwable, information: String = "null") {
        LOGGER.error("An uncaught exception has occurred: ($information)", throwable)
        val ex = throwable.stackTraceToString()
        val embed = WebhookEmbedBuilder.fromJDA(
            Embed(
                title = "An uncaught exception has occurred",
                color = ERROR_EMBED_COLOUR.rgb,
                description = ex.take(MessageEmbed.TEXT_MAX_LENGTH),
                timestamp = Instant.now(),
            )
        ).build()

        val message = WebhookMessageBuilder()
            .setAvatarUrl(avatarUrl)
            .addEmbeds(embed)
            .build()

        webhookClient.send(message)
    }

    override fun onDisable() {
        webhookClient.close()
    }
}
