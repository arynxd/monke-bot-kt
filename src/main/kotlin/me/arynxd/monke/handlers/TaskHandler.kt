package me.arynxd.monke.handlers

import me.arynxd.monke.launch.Monke
import me.arynxd.monke.objects.handlers.Handler
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class TaskHandler(
    override val monke: Monke
) : Handler() {
    private val tasks = mutableMapOf<String, Future<*>>()
    private val scheduler = monke.scheduler

    private fun addRepeatingTask(name: String, delay: Long, unit: TimeUnit, block: () -> Unit): ScheduledFuture<*> {
        val job = scheduler.scheduleAtFixedRate(block, 0, delay, unit)
        tasks[name] = job
        return job
    }

    fun addRepeatingTask(delay: Long, unit: TimeUnit, block: () -> Unit) =
        addRepeatingTask(getName(), delay, unit, block)

    fun addRepeatingTask(block: () -> Unit) =
        addRepeatingTask(getName(), 0, TimeUnit.MILLISECONDS, block)

    fun <T> addOneShot(delay: Long, unit: TimeUnit, name: String = getName(), block: () -> T): Future<T> {
        val job = scheduler.schedule(block, delay, unit)
        tasks[name] = job
        return job
    }

    private fun getName(): String {
        val name = UUID.randomUUID().toString()
        if (tasks.containsKey(name)) {
            return getName()
        }
        return name
    }

    fun removeTask(name: String) {
        tasks.remove(name)?.cancel(true)
    }

    override fun onDisable() {
        tasks.values.forEach { it.cancel(true) }
    }
}