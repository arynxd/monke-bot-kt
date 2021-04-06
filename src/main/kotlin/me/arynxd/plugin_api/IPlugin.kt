package me.arynxd.plugin_api

import me.arynxd.monke.Monke

interface IPlugin {
    fun onEnable(monke: Monke)
    fun onDisable()
}