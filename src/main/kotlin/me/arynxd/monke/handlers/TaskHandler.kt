package me.arynxd.monke.handlers

import me.arynxd.monke.Monke
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

    fun addTask(block: () -> Unit, name: String, delay: Long, unit: TimeUnit): ScheduledFuture<*> {
        val job = scheduler.scheduleAtFixedRate(block, 0, delay, unit)
        tasks[name] = job
        return job
    }

    fun addTask(block: () -> Unit, delay: Long, unit: TimeUnit) = addTask(block, getName(), delay, unit)

    fun addTask(block: () -> Unit) = addTask(block, getName(), 0, TimeUnit.MILLISECONDS)

    fun <T> addOneShot(block: () -> T, name: String, delay: Long, unit: TimeUnit): Future<T> {
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

    fun removeJob(name: String) {
        tasks.remove(name)?.cancel(true)
    }

    override fun onDisable() {
        tasks.values.forEach { it.cancel(true) }
    }
}