package me.arynxd.monke.handlers

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.arynxd.monke.launch.Monke
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

    fun getById(messageId: Long): Paginator? {
        return paginators.keys.firstOrNull { it.messageId == messageId }
    }

    fun remove(messageId: Long) {
        paginators.remove(getById(messageId))
    }

    override fun onDisable() {
        paginators.entries.forEach {
            it.key.delete()
            it.value.cancel(translateInternal { path = "cancel_reason.bot_shutdown" })
        }
    }

    override fun toString(): String {
        return paginators.entries.joinToString {
            "PAGINATOR -> ${it.key.messageId}"
        }
    }
}