package me.arynxd.monke.objects.handlers

import io.github.classgraph.ClassGraph
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.exception.HandlerNotFoundException
import okhttp3.OkHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Constructor
import java.util.*
import kotlin.system.exitProcess

const val HANDLER_PACKAGE = "me.arynxd.monke.handlers"

val LOGGER: Logger = LoggerFactory.getLogger(Monke::class.java)

class Handlers(val monke: Monke) {
    private val classGraph: ClassGraph = ClassGraph().acceptPackages(HANDLER_PACKAGE)
    private val handlers: Map<Class<*>, Handler> = loadHandlers()
    val okHttpClient: OkHttpClient = OkHttpClient()

    @Suppress("UNCHECKED_CAST")
    fun <T> get(clazz: Class<T>): T {
        if (!handlers.containsKey(clazz)) {
            throw HandlerNotFoundException("Handler '${clazz.simpleName}' was not found.")
        }
        return handlers[clazz] as T
    }

    private fun loadHandlers(): Map<Class<*>, Handler> {
        val handlers = mutableMapOf<Class<*>, Handler>()
        classGraph.scan().use { result ->
            for (cls in result.allClasses) {

                if (cls.isInnerClass) {
                    continue
                }

                val constructors: Array<Constructor<*>> = cls.loadClass().declaredConstructors

                if (constructors.isEmpty()) {
                    continue
                }

                val constructor =
                    constructors.firstOrNull { it.parameterCount == 1 && it.parameters[0].type == Monke::class.java }

                // These cannot be translated because the TranslationHandler has not been loaded yet
                if (constructor == null) {
                    LOGGER.warn("Non Handler class ( ${cls.simpleName} ) found in handlers package!")
                    continue
                }

                val instance: Any = constructor.newInstance(monke)
                if (instance !is Handler) {
                    LOGGER.warn("Non Handler class ( ${cls.simpleName} ) found in handlers package!")
                    continue
                }

                handlers[instance.javaClass] = instance
            }
        }

        return handlers.toMap()
    }


    fun enableHandlers() {
        val enabled = mutableListOf<Class<out Handler>>()
        val queue = LinkedList(handlers.values)
        var i = 0

        while (queue.isNotEmpty()) {
            val handler = queue.remove()
            if (i > 50) {
                // This cannot be translated because the TranslationHandler has not been loaded yet
                LOGGER.error("Suspected infinite loop while loading the handlers, closing.")
                exitProcess(1)
            }
            if (enabled.containsAll(handler.dependencies)) {
                enabled.add(handler.javaClass)
                handler.onEnable()
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
            LOGGER.info("Handler - ${it.javaClass.simpleName} stopping..")
            it.onDisable()
            LOGGER.info("Handler - ${it.javaClass.simpleName} stopped.")
        }
    }
}