package me.arynxd.monke.objects.plugins

import me.arynxd.plugin_api.IPlugin

class LoadedPlugin(
    val plugin: IPlugin,
    val config: PluginConfig
) {
    var isEnabled = true
}