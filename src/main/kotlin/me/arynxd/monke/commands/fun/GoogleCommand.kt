package me.arynxd.monke.commands.`fun`

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.handlers.LOGGER
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.net.URLEncoder

@Suppress("UNUSED")
class GoogleCommand : Command(
    name = "google",
    description = "Queries Google with the given text.",
    category = CommandCategory.FUN,
    aliases = listOf("g"),
    cooldown = 3000L,

    arguments = ArgumentConfiguration(
        listOf(
            ArgumentString(
                name = "query",
                description = "The search query.",
                required = true,
                type = ArgumentType.VARARG,
                condition = { it.isNotBlank() }
            )
        )
    )
) {
    override suspend fun run(event: CommandEvent) = withContext(Dispatchers.IO) {
        val query = URLEncoder.encode(event.getVararg<String>(0).joinToString(" "), "utf-8")
        val language = event.getLanguage()

        try {
            val url = "https://www.google.com/search?q=$query&safe=active&hl=en"
            val doc = Jsoup.connect(url).get()

            val links: Elements = doc.select(".yuRUbf").select("a")
            val names: Elements = doc.select(".yuRUbf").select("a").select("h3")
            val descriptions: Elements = doc.select(".IsZvec").select(".aCOpRe").select("span")

            if (links.isEmpty() || names.isEmpty() || descriptions.isEmpty()) {
                event.replyAsync {
                    exception()
                    title(
                        TranslationHandler.getString(
                            language = language,
                            key = "command.google.response.no_results"
                        )
                    )
                    footer()
                    send()
                }
                return@withContext
            }

            event.replyAsync {
                success()
                description("${links[0].attr("href")} --> *${names[0].text()}* \n\n ${descriptions[0].text()}")
                footer()
                send()
            }

        } catch (exception: Exception) {
            val error = TranslationHandler.getString(language, "internal_error.web_service_error", "Google")
            event.replyAsync {
                exception()
                title(
                    TranslationHandler.getString(
                        language = language,
                        key = "command.google.response.no_results"
                    )
                )
                footer()
                send()
            }
            LOGGER.error(error, exception)
        }
    }
}