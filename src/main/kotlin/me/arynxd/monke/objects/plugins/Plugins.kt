package me.arynxd.monke.objects.plugins

import me.arynxd.monke.Monke
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.util.convertToString
import me.arynxd.plugin_api.IPlugin
import java.io.File
import java.net.URL
import java.util.jar.JarFile
import java.net.URLClassLoader

class Plugins(val monke: Monke) {
    private val plugins = mutableListOf<IPlugin>()

    fun load() {
        val pluginsFolder = File("plugins")

        if (!pluginsFolder.exists()) {
            LOGGER.info("Plugins folder did not exist, aborting plugin loading")
            pluginsFolder.mkdir()
            return
        }

        if (!pluginsFolder.isDirectory) {
            LOGGER.info("Plugins folder was not a folder, aborting plugin loading")
            return
        }

        val files = pluginsFolder.listFiles()!!.filter { it.name.endsWith(".jar") }

        if (files.isEmpty()) {
            LOGGER.info("Plugins folder had no jar files inside")
            return
        }

        for (file in files) {
            val jarPath = file.path
            val pluginName = file.name
            val jarFile = JarFile(jarPath)

            jarFile.use { jar ->
                val loadedMain = getMainClass(jar, jarPath)

                if (loadedMain == null) {
                    LOGGER.warn("Could not load main class for plugin '$pluginName'")
                    return@use
                }

                val constructors = loadedMain.constructors
                val constructor = constructors.find { it.parameters.isEmpty() }

                if (constructors.isEmpty() || constructor == null) {
                    LOGGER.warn("No constructors found for plugin '$pluginName'")
                    return@use
                }

                val main = constructor.newInstance()

                if (main !is IPlugin) {
                    LOGGER.warn("Main class for plugin '$pluginName' was not valid")
                    return@use
                }

                main.onEnable(monke)
                plugins.add(main)
            }
        }
    }

    private fun getMainClass(file: JarFile, jarPath: String): Class<*>? {
        val entries = file.entries()
        val mainClassEntry = file.getJarEntry("main.txt") ?: return null

        val mainClassName = convertToString(file.getInputStream(mainClassEntry))
        val classLoader = URLClassLoader.newInstance(arrayOf(URL("jar:file:$jarPath!/")))

        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            if (entry.realName == mainClassName) {
                val loaderName = entry.name
                    .substring(0, mainClassName.length - 6)
                    .replace('/', '.')

                return try {
                    classLoader.loadClass(loaderName)
                }
                catch (exception: ClassNotFoundException) {
                    null
                }
            }
        }
        return null
    }
}