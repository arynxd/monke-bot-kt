package me.arynxd.monke.objects.web

import net.dv8tion.jda.api.utils.data.DataObject
import java.time.OffsetDateTime

const val WIKIPEDIA_API = "https://en.wikipedia.org/api/rest_v1/page/summary/"

class WikipediaPage(val dataObject: DataObject) {
    fun getType(): PageType? =
        if (!dataObject.hasKey("type")) null
        else PageType.values().firstOrNull { page -> page.param.equals(dataObject.getString("type"), true) }
            ?: PageType.ERROR

    fun getTimestamp(): OffsetDateTime? =
        if (dataObject.hasKey("timestamp")) OffsetDateTime.parse(dataObject.getString("timestamp")) else null

    fun getExtract(): String? = dataObject.getString("extract", null)

    fun getTitle(): String? = dataObject.getString("displaytitle", null)

    fun getThumbnail(): String? = if (dataObject.hasKey("thumbnail")) {
        if (dataObject.getObject("thumbnail")
                .hasKey("source")
        ) dataObject.getObject("thumbnail").getString("source")
        else null
    }
    else null

    enum class PageType(val param: String) {
        DISAMBIGUATION("disambiguation"),
        STANDARD("standard"),
        NOT_FOUND("https://mediawiki.org/wiki/HyperSwitch/errors/not_found"),
        ERROR("")
    }
}