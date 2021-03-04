package me.arynxd.monke.handlers

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.Paginator
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.translation.Language

class PaginationHandler @JvmOverloads constructor(
    override val monke: Monke,
    override val dependencies: List<Class<out Handler>> = listOf(TranslationHandler::class.java)
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
                it.value.cancel(TranslationHandler.getString(Language.EN_US, "cancel_reason.timeout"))
            }
    }

    override fun onDisable() {
        paginators.entries.forEach {
            it.key.delete()
            it.value.cancel(TranslationHandler.getString(Language.EN_US, "cancel_reason.bot_shutdown"))
        }
    }
}