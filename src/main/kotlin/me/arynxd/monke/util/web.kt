package me.arynxd.monke.util

import dev.minn.jda.ktx.Embed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.handlers.LOGGER
import me.arynxd.monke.objects.web.RedditPost
import me.arynxd.monke.objects.web.WIKIPEDIA_API
import me.arynxd.monke.objects.web.WikipediaPage
import net.dv8tion.jda.api.utils.data.DataObject
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import ru.gildor.coroutines.okhttp.await
import java.io.Reader

const val HASTEBIN_SERVER = "https://hastebin.monkebot.ml/"

suspend fun getPosts(subreddit: String, monke: Monke): List<RedditPost> {
    val request: Request = Request.Builder()
        .url("https://www.reddit.com/r/$subreddit/.json")
        .build()

    val response = monke.handlers.okHttpClient.newCall(request).await()
    val body = response.body()
    val posts: MutableList<RedditPost> = mutableListOf()

    if (!response.isSuccessful || body == null) {
        val error = TranslationHandler.getInternalString("internal_error.web_service_error", "Reddit")
        LOGGER.error(error)
        return emptyList()
    }

    val redditJson: DataObject = DataObject.fromJson(withContext(Dispatchers.IO) { body.string() })
    if (!redditJson.hasKey("data") && !redditJson.getObject("data").hasKey("children")) {
        return emptyList()
    }

    val jsonArray = redditJson.getObject("data").getArray("children")

    for (i in 0 until jsonArray.length()) {
        val meme = jsonArray.getObject(i)
        if (!meme.hasKey("data"))
            continue

        posts.add(RedditPost(meme.getObject("data")))
    }

    return posts
}

fun checkAndSendPost(event: CommandEvent, post: RedditPost) {
    val language = event.getLanguage()
    val error = TranslationHandler.getString(language, "command_error.nsfw_reddit_post")
    if (event.channel.isNSFW && (post.isNSFW() != false || post.isSpoiled() != false)) {
        sendError(event.message, error)
    }

    val description = TranslationHandler.getString(
        language, "command_response.reddit_description",
        post.getSubreddit() ?: "null",
        post.getAuthor() ?: "null"
    )

    val footer = TranslationHandler.getString(
        language, "command_response.reddit_footer",
        post.getUpvotes() ?: "0",
        post.getDownvotes() ?: "0"
    )

    event.sendEmbed(
        Embed(
            title = post.getTitle(),
            description = description,
            image = post.getURL(),
            footerText = footer
        )
    )
}

suspend fun getWikipediaPage(event: CommandEvent, subject: String): WikipediaPage? {
    val request: Request = Request.Builder()
        .url(WIKIPEDIA_API + subject)
        .build()

    val response = event.monke.handlers.okHttpClient.newCall(request).await()
    val body = response.body()

    if (!response.isSuccessful || body == null) {
        val error = TranslationHandler.getInternalString("internal_error.web_service_error", "Wikipedia")
        LOGGER.error(error)
        return null
    }

    val page = WikipediaPage(DataObject.fromJson(withContext(Dispatchers.IO) { body.string() }))

    return page.takeIf { it.getType() == WikipediaPage.PageType.STANDARD }
}

suspend fun postBin(text: String, client: OkHttpClient): String? {
    return withContext(Dispatchers.IO) {
        client.newCall(
            Request.Builder()
                .post(RequestBody.create(MediaType.parse("text/plain"), text))
                .url(HASTEBIN_SERVER + "documents")
                .header("User-Agent", "Mozilla/5.0 Monke")
                .build()
        ).execute().let { response ->
            if (!response.isSuccessful)
                null
            else
                HASTEBIN_SERVER + DataObject.fromJson(
                    response.body()?.charStream() ?: Reader.nullReader()
                ).getString("key")
        }
    }
}
