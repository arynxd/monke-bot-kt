package me.arynxd.monke.objects.plugins

import me.arynxd.plugin_api.IPlugin

data class LoadedPlugin(
    val plugin: IPlugin,
    val config: PluginConfig,
    var isEnabled: Boolean = true
)