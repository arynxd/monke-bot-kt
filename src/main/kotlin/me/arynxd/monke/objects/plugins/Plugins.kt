package me.arynxd.monke.objects.plugins

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.arynxd.monke.launch.Monke
import me.arynxd.monke.handlers.ExceptionHandler
import me.arynxd.monke.handlers.translation.translateAllInternal
import me.arynxd.monke.handlers.translation.translateInternal
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.util.readFully
import me.arynxd.plugin_api.IPlugin
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.jar.JarFile

class Plugins(val monke: Monke) {
    private val plugins = ConcurrentHashMap<String, LoadedPlugin>()
    private val pluginsFolder = File("plugins")
    private val pool = Executors.newFixedThreadPool(10) { Thread(it, "Monke-Plugin-Thread") }

    fun loadPlugins() {
        if (!pluginsFolder.exists()) {
            LOGGER.info(
                translateInternal { path = "plugin.folder_not_found" }
            )
            pluginsFolder.mkdir()
            return
        }

        if (!pluginsFolder.isDirectory) {
            LOGGER.info(
                translateInternal { path = "plugin.folder_not_folder" }
            )
            return
        }

        val files = pluginsFolder.listFiles()!!.filter { it.name.endsWith(".jar") }

        if (files.isEmpty()) {
            LOGGER.info(
                translateInternal { path = "plugin.folder_empty" }
            )
            return
        }

        for (file in files) {
            val jarPath = file.path
            val fileName = file.name.substring(0, file.name.length - ".jar".length)

            translateInternal { path = "plugin.loading_jar"; values = arrayOf(fileName) }
            LOGGER.info(
                translateInternal { path = "plugin.loading_jar"; values = arrayOf(fileName) }
            )
            val jarFile = JarFile(jarPath)

            jarFile.use { jar ->
                val pluginInfo = getPluginInfo(jar, jarPath)

                val config = pluginInfo.first
                val mainClass = pluginInfo.second

                if (config == null) {
                    LOGGER.warn(
                        translateInternal { path = "plugin.config_not_found"; values = arrayOf(fileName) }
                    )
                    return@use
                }

                if (mainClass == null) {
                    LOGGER.warn(
                        translateInternal { path = "plugin.class_not_found"; values = arrayOf(fileName) }
                    )
                    return@use
                }

                if (!IPlugin::class.java.isAssignableFrom(mainClass)) {
                    LOGGER.warn(
                        translateInternal { path = "plugin.invalid_class"; values = arrayOf(fileName, "IPlugin") }
                    )
                    return@use
                }

                val constructor = mainClass.constructors.find { it.parameters.isEmpty() }

                if (constructor == null) {
                    LOGGER.warn(
                        translateInternal { path = "plugin.constructor_not_found"; values = arrayOf(fileName) }
                    )
                    return@use
                }

                val mainInstance = constructor.newInstance() as IPlugin

                pool.submit {
                    tryEnablePlugin(mainInstance, config, fileName)
                }
            }
        }
    }

    private fun tryEnablePlugin(plugin: IPlugin, config: PluginConfig, fileName: String) {
        try {
            plugin.onEnable(monke)
        }
        catch (exception: Exception) {
            LOGGER.error(
                translateInternal {
                    path = "plugin.uncaught_exception_boot"; values = arrayOf(fileName, config.version)
                },
                exception
            )
            plugin.onDisable()
            monke.handlers[ExceptionHandler::class]
                .handle(exception, translateInternal { path = "plugin.uncaught_exception" })
            return
        }

        if (plugins.containsKey(config.name)) {
            LOGGER.warn(
                translateInternal { path = "plugin.duplicate_plugin"; values = arrayOf(config.name) }
            )
            return
        }

        plugins[config.name] = LoadedPlugin(plugin, config)
        LOGGER.info(
            translateInternal {
                path = "plugin.plugin_loaded"
                values = arrayOf(fileName, config.name, config.version)
            }
        )
    }

    fun reload() {
        LOGGER.info(
            translateInternal { path = "plugin.reloading" }
        )
        disablePlugins()
        plugins.clear() //Drop all references to the plugin classes to avoid leaks:tm:
        loadPlugins()
    }

    fun disablePlugins() {
        plugins.forEach {
            val (disabling, disabled, err) = translateAllInternal {
                partInternal("plugin.disabling_plugin", it.key)
                partInternal("plugin.disabled_plugin", it.key)
                partInternal("plugin.uncaught_exception_shutdown", it.key)
            }
            LOGGER.info(disabling)
            try {
                it.value.plugin.onDisable()
            }
            catch (ex: Exception) {
                LOGGER.error(err, ex)
            }
            LOGGER.info(disabled)
        }
    }

    fun getByName(name: String) = plugins[name]

    //Gets information about a given plugin (Config file and Main class)
    //Returns null in both elements if the config file cannot be loaded
    //Returns null in the second element if the main class cannot be loaded, but the config file loaded ok
    private fun getPluginInfo(file: JarFile, jarPath: String): Pair<PluginConfig?, Class<*>?> {
        val entries = file.entries()
        val mainClassEntry = file.getJarEntry("plugin.json") ?: return Pair(null, null)
        val parser = Json { isLenient = true }

        val config = try {
            parser.decodeFromString<PluginConfig>(file.getInputStream(mainClassEntry).readFully())
        }
        catch (exception: Exception) {
            return Pair(null, null)
        }

        val classLoader = URLClassLoader(arrayOf(URL("jar:file:$jarPath!/")), javaClass.classLoader)

        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            if (entry.realName == config.mainClass) {
                val loaderName = entry.name
                    .substring(0, config.mainClass.length - 6) //Remove the .class extension
                    .replace('/', '.') //Form it into a usable path

                return try {
                    Pair(config, classLoader.loadClass(loaderName))
                }
                catch (exception: ClassNotFoundException) {
                    Pair(config, null)
                }
            }
        }
        return Pair(config, null)
    }

    fun getPluginList(): String {
        val (enabled, disabled) = translateAllInternal {
            partInternal("keyword.enabled")
            partInternal("keyword.disabled")
        }
        return plugins.values.joinToString {
            "**${it.config.name} (${it.config.version})** -> ${if (it.isEnabled) enabled else disabled}\n"
        }
    }
}

@Serializable
data class PluginConfig(
    val mainClass: String,
    val name: String,
    val version: Double
)
