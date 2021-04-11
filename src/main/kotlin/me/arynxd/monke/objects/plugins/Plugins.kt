package me.arynxd.monke.objects.plugins

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.ExceptionHandler
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.util.convertToString
import me.arynxd.plugin_api.IPlugin
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarFile

class Plugins(val monke: Monke) {
    private val plugins = ConcurrentHashMap<String, LoadedPlugin>()
    private val pluginsFolder = File("plugins")

    fun load() {
        if (!pluginsFolder.exists()) {
            LOGGER.info("Plugin - plugins folder did not exist, aborting plugin loading")
            pluginsFolder.mkdir()
            return
        }

        if (!pluginsFolder.isDirectory) {
            LOGGER.info("Plugin - plugins folder was not a folder, aborting plugin loading")
            return
        }

        val files = pluginsFolder.listFiles()!!.filter { it.name.endsWith(".jar") }

        if (files.isEmpty()) {
            LOGGER.info("Plugin - plugins folder had no jar files inside")
            return
        }

        for (file in files) {
            val jarPath = file.path
            val pluginName = file.name.substring(0, file.name.length - ".jar".length)

            LOGGER.info("Plugin - loading plugin $pluginName.jar")
            val jarFile = JarFile(jarPath)

            jarFile.use { jar ->
                val pluginInfo = getPluginInfo(jar, jarPath)

                val config = pluginInfo.first
                val mainClass = pluginInfo.second

                if (config == null) {
                    LOGGER.warn("Plugin - could not load config for plugin '$pluginName.jar' check the config and try again.")
                    return@use
                }

                if (mainClass == null) {
                    LOGGER.warn("Plugin - could not load main class for plugin '$pluginName.jar' check the config and try again.")
                    return@use
                }

                if (!IPlugin::class.java.isAssignableFrom(mainClass)) {
                    LOGGER.warn("Plugin - main class for plugin '$pluginName.jar' does not implement IPlugin or a subtype of it.")
                    return@use
                }

                val constructor = mainClass.constructors.find { it.parameters.isEmpty() }

                if (constructor == null) {
                    LOGGER.warn("Plugin - no empty constructors found for plugin '$pluginName.jar'")
                    return@use
                }

                val mainInstance = constructor.newInstance() as IPlugin

                tryEnablePlugin(mainInstance, pluginName, config)
            }
        }
    }

    private fun tryEnablePlugin(plugin: IPlugin, fileName: String, config: PluginConfig) {
        try {
            plugin.onEnable(monke)
        }
        catch (exception: Exception) {
            LOGGER.error(
                "Plugin - plugin '$fileName.jar' had an uncaught error at startup. Is it up to date? Current version: (${config.version})",
                exception
            )
            plugin.onDisable()
            monke.handlers.get(ExceptionHandler::class)
                .handle(exception, "A plugin had an uncaught exception")
            return
        }

        if (plugins.containsKey(config.name)) {
            LOGGER.warn("Duplicate plugin '${config.name}', skipping.")
            return
        }

        plugins[config.name] = LoadedPlugin(plugin, config)
        LOGGER.info("Plugin - loaded plugin $fileName.jar (${config.name} ${config.version})")
    }

    fun reload() {
        LOGGER.info("Plugin - reloading all plugins")
        disable()
        plugins.clear()
        load()
    }

    fun disable() {
        plugins.forEach {
            LOGGER.info("Plugin - disabling plugin ${it.key}")
            it.value.plugin.onDisable()
            LOGGER.info("Plugin - disabled plugin ${it.key}")
        }
    }

    fun getByName(name: String) = plugins[name]

    //Gets information about a given plugin (Config file and Main class)
    //Returns null in both elements if the config file cannot be loaded
    //Returns null in the second element if the main class cannot be loaded, but the config file loaded ok
    private fun getPluginInfo(file: JarFile, jarPath: String): Pair<PluginConfig?, Class<*>?> {
        val entries = file.entries()
        val mainClassEntry = file.getJarEntry("plugin.json") ?: return Pair(null, null)

        val config = try {
                Json { isLenient = true }.decodeFromString<PluginConfig>(convertToString(file.getInputStream(mainClassEntry)))
            }
            catch (exception: Exception) {
                return Pair(null, null)
            }

        val classLoader = URLClassLoader(arrayOf(URL("jar:file:$jarPath!/")), javaClass.classLoader)

        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            if (entry.realName == config.mainClass) {
                val loaderName = entry.name
                    .substring(0, config.mainClass.length - 6)
                    .replace('/', '.')

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
        return plugins.values.joinToString {
            "**${it.config.name} (${it.config.version})** -> ${if (it.isEnabled) "Enabled" else "Disabled"}\n"
        }
    }
}

@Serializable
data class PluginConfig(
    val mainClass: String,
    val name: String,
    val version: Double
)