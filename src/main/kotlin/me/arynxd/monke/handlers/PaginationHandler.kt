package me.arynxd.monke.handlers

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.arynxd.monke.Monke
import me.arynxd.monke.util.Paginator
import me.arynxd.monke.objects.handlers.Handler
import kotlin.reflect.KClass

class PaginationHandler(
    override val monke: Monke,
    override val dependencies: List<KClass<out Handler>> = listOf(TranslationHandler::class)
) : Handler() {
    private val paginators: MutableMap<Paginator, Job> = mutableMapOf()

    fun addPaginator(paginator: Paginator) {
        paginators[paginator] = GlobalScope.launch {
            paginator.paginate()
        }
    }

    fun cleanup() {
        paginators.entries
            .filter { (it.key.lastUsed + 30_000) < System.currentTimeMillis() } //Has the paginator been left for 30 seconds
            .forEach {
                it.key.delete()
                it.value.cancel(translateInternal("cancel_reason.timeout"))
            }
    }

    override fun onDisable() {
        paginators.entries.forEach {
            it.key.delete()
            it.value.cancel(translateInternal("cancel_reason.bot_shutdown"))
        }
    }
}