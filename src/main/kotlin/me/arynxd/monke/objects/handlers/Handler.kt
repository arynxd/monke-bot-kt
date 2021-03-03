package me.arynxd.monke.objects.handlers

import me.arynxd.monke.Monke

interface Handler {
    fun onEnable()
    fun onDisable()
    val monke: Monke
    val dependencies: List<Class<out Handler>>
}