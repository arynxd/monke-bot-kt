package me.arynxd.monke.objects.handlers

import me.arynxd.monke.Monke
import net.dv8tion.jda.api.hooks.ListenerAdapter
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties

abstract class Handler : ListenerAdapter() {

    open fun onEnable() {
        //Placeholder method
    }

    open fun onDisable() {
        //Placeholder method
    }

    fun loadProps() {
        this::class.declaredMemberProperties
            .mapNotNull { getDelegate(it) }
            .filterIsInstance<whenEnabled<*>>()
            .forEach { it.load() }
    }

    private fun <T> getDelegate(prop: KProperty<T>): Any? {
        return try {
            javaClass.getDeclaredField("${prop.name}\$delegate").let {
                it.isAccessible = true
                it.get(this)
            }
        }
        catch (exception: NoSuchFieldException) {
            null
        }
    }

    abstract val monke: Monke
    open val dependencies: List<KClass<out Handler>> = emptyList()
}