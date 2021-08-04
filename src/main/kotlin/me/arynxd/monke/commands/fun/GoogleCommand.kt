package me.arynxd.monke.commands.`fun`

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.arynxd.monke.handlers.translation.translate
import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.impl.ArgumentString
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.command.threads.CommandReply
import org.jsoup.Jsoup
import java.net.URLEncoder

@Suppress("UNUSED")
class GoogleCommand : Command(
    CommandMetaData(
        name = "google",
        description = "Queries Google with the given text.",
        category = CommandCategory.FUN,
        aliases = listOf("g"),
        flags = listOf(CommandFlag.SUSPENDING),
        cooldown = 3000L,

        arguments = ArgumentConfiguration(
            ArgumentString(
                name = "query",
                description = "The search query.",
                required = true,
                type = Argument.Type.VARARG,
            )
        )
    )
) {
    override suspend fun runSuspend(event: CommandEvent) = withContext(Dispatchers.IO) {
        val query = URLEncoder.encode(event.vararg<String>(0).joinToString(" "), "utf-8")
        val language = event.language

        val url = "https://www.google.com/search?q=$query&safe=active&hl=en"
        val doc = Jsoup.connect(url).get()

        //TODO: get a better way of doing this, maybe config or something
        val links = doc
            .select(".yuRUbf")
            .select("a")

        val names = doc
            .select(".yuRUbf")
            .select("a").select("h3")

        val descriptions = doc
            .select(".IsZvec")
            .select(".VwiC3b")
            .select(".yXK7lf")
            .select(".MUxGbd")
            .select(".yDYNvb")
            .select(".lyLwlc")
            .select("span")

        if (links.isEmpty() || names.isEmpty() || descriptions.isEmpty()) {
            event.replyAsync {
                type(CommandReply.Type.EXCEPTION)
                title(
                    translate {
                        lang = language
                        path = "command.google.response.no_results"
                    }
                )
                footer()
                event.thread.post(this)
            }
            return@withContext
        }

        event.replyAsync {
            type(CommandReply.Type.SUCCESS)
            title("*${names[0].text()}*")
            description("${links[0].attr("href")} \n\n ${descriptions[0].text()}")
            footer()
            event.thread.post(this)
        }
    }
}