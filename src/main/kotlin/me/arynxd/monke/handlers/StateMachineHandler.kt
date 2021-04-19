package me.arynxd.monke.handlers

import me.arynxd.monke.Monke
import me.arynxd.monke.objects.StateMachine
import me.arynxd.monke.objects.handlers.Handler
import net.dv8tion.jda.api.events.GenericEvent

@Suppress("UNUSED")
class StateMachineHandler(override val monke: Monke) : Handler() {
    private val machines = mutableListOf<StateMachine<*>>()
    private val toRemove = mutableListOf<StateMachine<*>>()

    fun addMachine(machine: StateMachine<*>) {
        machines.add(machine)
    }

    fun removeMachine(machine: StateMachine<*>) {
        toRemove.add(machine)
    }

    override fun onGenericEvent(event: GenericEvent) {
        machines.forEach { it.onEvent(event) }
        machines.removeAll(toRemove)
        toRemove.clear()
    }
}