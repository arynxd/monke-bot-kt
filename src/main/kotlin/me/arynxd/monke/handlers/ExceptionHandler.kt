package me.arynxd.monke.handlers

import me.arynxd.monke.Monke
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.handlers.LOGGER
import kotlin.reflect.KClass

@Suppress("UNUSED")
class ExceptionHandler @JvmOverloads constructor(
    override val monke: Monke,
    override val dependencies: List<KClass<out Handler>> = listOf(ConfigHandler::class)
) : Handler() {
    override fun onEnable() {
        webhookUrl = monke.handlers.get(ConfigHandler::class).config.logWebhook
    }

    companion object {
        private lateinit var webhookUrl: String

        fun handle(throwable: Throwable, information: String = "null") {
            LOGGER.error("An uncaught exception has occurred: ($information)", throwable)
        }
    }
}
