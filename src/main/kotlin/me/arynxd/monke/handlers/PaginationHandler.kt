package me.arynxd.monke.handlers

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.translation.TranslationHandler
import me.arynxd.monke.handlers.translation.translateInternal
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.util.classes.Paginator
import kotlin.reflect.KClass

class PaginationHandler(
    override val monke: Monke,
    override val dependencies: List<KClass<out Handler>> = listOf(TranslationHandler::class)
) : Handler() {
    private val paginators: MutableMap<Paginator, Job> = mutableMapOf()

    fun addPaginator(paginator: Paginator) {
        paginators[paginator] = monke.coroutineScope.launch {
            paginator.paginate()
        }
    }

    override fun onDisable() {
        paginators.entries.forEach {
            it.key.delete()
            it.value.cancel(translateInternal { path = "cancel_reason.bot_shutdown" })
        }
    }
}