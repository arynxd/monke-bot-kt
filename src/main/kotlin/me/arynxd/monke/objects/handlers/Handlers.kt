package me.arynxd.monke.objects.handlers

import me.arynxd.monke.Monke
import me.arynxd.monke.objects.exception.HandlerException
import okhttp3.OkHttpClient
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.reflect.KClass
import kotlin.system.exitProcess

const val HANDLER_PACKAGE = "me.arynxd.monke.handlers"

val LOGGER: Logger = LoggerFactory.getLogger(Monke::class.java)

class Handlers(val monke: Monke) {
    private val reflections = Reflections(HANDLER_PACKAGE, SubTypesScanner())

    val handlers: Map<KClass<*>, Handler> = loadHandlers()
    val okHttpClient: OkHttpClient = OkHttpClient()

    fun <T : Any> get(clazz: KClass<T>): T {
        if (!handlers.containsKey(clazz)) {
            throw HandlerException("Handler '${clazz.simpleName}' was not found.")
        }
        @Suppress("UNCHECKED_CAST")
        return handlers[clazz] as? T
            ?: throw IllegalArgumentException("Class $clazz is not mapped to a valid handler")
    }

    private fun loadHandlers(): Map<KClass<*>, Handler> {
        val handlers = mutableMapOf<KClass<*>, Handler>()

        val classes = reflections.getSubTypesOf(Handler::class.java)

        for (cls in classes) {
            val constructor =
                cls.constructors.firstOrNull { it.parameterCount == 1 && it.parameters[0].type == Monke::class.java }

            // These cannot be translated because the TranslationHandler has not been loaded yet
            if (constructor == null) {
                LOGGER.warn("Non Handler class ( ${cls.simpleName} ) found in handlers package!")
                continue
            }

            val instance = constructor.newInstance(monke)

            if (instance !is Handler) {
                LOGGER.warn("Non Handler class ( ${cls.simpleName} ) found in handlers package!")
                continue
            }

            handlers[instance::class] = instance
        }

        return Collections.unmodifiableMap(handlers.toMap())
    }

    fun enableHandlers() {
        val enabled = mutableListOf<KClass<out Handler>>()
        val queue = LinkedList(handlers.values)
        var i = 0

        while (queue.isNotEmpty()) {
            val handler = queue.remove()
            if (i > 50) {
                // This cannot be translated because the TranslationHandler may not be loaded yet
                LOGGER.error("Suspected infinite loop while loading the handlers, closing.")
                exitProcess(1)
            }
            if (enabled.containsAll(handler.dependencies)) {
                enabled.add(handler::class)
                try {
                    handler.loadProps()
                    handler.onEnable()
                }
                catch (exception: Exception) {
                    LOGGER.error("A handler had an uncaught exception whilst enabling", exception)
                    exitProcess(1)
                }
                i = 0
                continue
            }
            queue.add(handler)
            i++
        }

        LOGGER.info("Successfully enabled all handlers!")
    }

    fun disableHandlers() {
        handlers.values.forEach {
            it.onDisable()
            LOGGER.info("Handler - ${it.javaClass.simpleName} stopped.")
        }
    }
}