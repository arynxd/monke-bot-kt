package me.arynxd.monke.util

import me.arynxd.monke.MONKE_VERSION
import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.translation.translate
import me.arynxd.monke.handlers.translation.translateInternal
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.objects.web.RedditPost
import me.arynxd.monke.objects.web.WIKIPEDIA_API
import me.arynxd.monke.objects.web.WikipediaPage
import net.dv8tion.jda.api.utils.data.DataObject
import okhttp3.*
import org.jsoup.UncheckedIOException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.gildor.coroutines.okhttp.await
import java.io.Closeable
import java.io.IOException

const val HASTEBIN_SERVER = "https://hastebin.monkebot.ml/"

fun Call.asMonoEnqueued(): Mono<Response> = Mono.create { sink ->
    this.enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) = sink.error(e)
        override fun onResponse(call: Call, response: Response) = sink.success(response)
    })
}

fun <T : Closeable, S : Closeable> T.useWith(other: S, fn: (T, S) -> Unit) {
    this.use { me ->
        other.use { o ->
            fn(me, o)
        }
    }
}

fun getPosts(subreddit: String, monke: Monke): Flux<RedditPost> {
    val request = Request.Builder()
        .url("https://www.reddit.com/r/$subreddit/.json")
        .addHeader("User-Agent", "Monkebot/${MONKE_VERSION} (Discord bot)")
        .build()

    return monke.handlers.okHttpClient.newCall(request).asMonoEnqueued()
        .flux()
        .flatMap { resp ->
            val body = resp.body()

            val err = translateInternal {
                path = "internal_error.web_service_error"
                values = arrayOf("Reddit")
            }

            val ioEx = UncheckedIOException(err)

            Flux.create { sink ->
                if (body == null) {
                    LOGGER.error(err)
                    sink.error(ioEx)
                    return@create
                }

                resp.useWith(body) { res, bdy ->
                    if (!res.isSuccessful) {
                        return@useWith
                    }

                    val redditJson = DataObject.fromJson(bdy.string())

                    if (!redditJson.hasKey("data")) {
                        sink.error(ioEx)
                        return@useWith
                    }

                    if (!redditJson.getObject("data").hasKey("children")) {
                        sink.error(ioEx)
                        return@useWith
                    }

                    val jsonArray = redditJson
                        .getObject("data")
                        .getArray("children")

                    for (i in 0 until jsonArray.length()) {
                        val post = jsonArray.getObject(i)
                        if (!post.hasKey("data")) {
                            continue
                        }

                        sink.next(RedditPost(post.getObject("data")))
                    }
                }
            }
        }
}

fun checkAndSendPost(event: CommandEvent, post: RedditPost) {
    val language = event.language
    if (!event.channel.isNSFW && (post.isNSFW() != false || post.isSpoiled() != false)) {
        event.replyAsync {
            type(CommandReply.Type.EXCEPTION)
            title(
                translate {
                    lang = language
                    path = "command_error.nsfw_reddit_post"
                }
            )
            footer()
            event.thread.post(this)
        }
        return
    }

    val description = translate {
        lang = language
        path = "command_response.reddit_description"
        values = arrayOf(
            post.getSubreddit().toString(),
            post.getAuthor().toString()
        )
    }

    val footer = translate {
        lang = language
        path = "command_response.reddit_footer"
        values = arrayOf(
            post.getUpvotes() ?: "0",
            post.getDownvotes() ?: "0"
        )
    }

    event.replyAsync {
        type(CommandReply.Type.SUCCESS)
        title(post.getTitle())
        description(description)
        image(post.getURL())
        footer(footer)
        event.thread.post(this)
    }
}

fun getWikipediaPage(event: CommandEvent, subject: String): Mono<WikipediaPage> {
    val request = Request.Builder()
        .url(WIKIPEDIA_API + subject)
        .build()

    val err = translateInternal {
        path = "internal_error.web_service_error"
        values = arrayOf("Wikipedia")
    }

    val ioEx = UncheckedIOException(err)

    return Mono.create { sink ->
        event.monke.handlers.okHttpClient.newCall(request).asMonoEnqueued()
            .map { resp ->
                val body = resp.body()

                if (body == null) {
                    sink.error(ioEx)
                }
                else {
                    resp.useWith(body) { _, bdy ->
                        WikipediaPage(DataObject.fromJson(bdy.string()))
                    }
                }
            }
    }
}

suspend fun postBin(text: String, client: OkHttpClient): String? {
    val request = Request.Builder()
        .post(RequestBody.create(MediaType.parse("text/plain"), text))
        .url(HASTEBIN_SERVER + "documents")
        .header("User-Agent", "Mozilla/5.0 Monke")
        .build()

    client.newCall(request).await().use { resp ->
        if (!resp.isSuccessful) {
            return null
        }

        val charStream = resp.body() ?: return null

        charStream.use { body ->
            val json = DataObject.fromJson(body.charStream())

            if (!json.hasKey("key")) {
                return null
            }

            return HASTEBIN_SERVER + json.getString("key")
        }
    }
}
