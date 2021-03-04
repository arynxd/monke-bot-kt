package me.arynxd.monke.objects.handlers

import me.arynxd.monke.Monke
import net.dv8tion.jda.api.hooks.ListenerAdapter

abstract class Handler: ListenerAdapter() {
    open fun onEnable() {
        //Placeholder method
    }
    open fun onDisable() {
        //Placeholder method
    }

    abstract val monke: Monke
    open val dependencies: List<Class<out Handler>> = emptyList()
}