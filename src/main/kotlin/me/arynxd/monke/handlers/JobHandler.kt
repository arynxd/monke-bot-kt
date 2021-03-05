package me.arynxd.monke.handlers

import kotlinx.coroutines.*
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.handlers.Handler
import java.util.*

class JobHandler(
    override val monke: Monke
) : Handler() {
    private val jobs = mutableMapOf<String, Job>()

    fun addJob(block: suspend CoroutineScope.() -> Unit, name: String = getName()): Job {
        val job = GlobalScope.launch(block = block)
        jobs[name] = job
        return job
    }

    private fun getName(): String {
        val name = UUID.randomUUID().toString()
        if (jobs.containsKey(name)) {
            return getName()
        }
        return name
    }

    fun removeJob(name: String) {
        jobs.remove(name)?.cancel("Manually cancelled")
    }

    override fun onDisable() {
        jobs.values.forEach { it.cancel(TranslationHandler.getInternalString("cancel_reason.bot_shutdown")) }
    }
}