package me.arynxd.monke.commands.`fun`

import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.util.checkAndSendPost
import me.arynxd.monke.util.getPosts
import kotlin.random.Random

@Suppress("UNUSED")
class CatCommand : Command(
    name = "cat",
    description = "Shows cute cats from Reddit.",
    category = CommandCategory.FUN,
    aliases = listOf("kitty"),
    flags = listOf(CommandFlag.ASYNC),
    cooldown = 3000L,

    ) {

    override suspend fun runSuspend(event: CommandEvent) {
        val subreddits = listOf("kittens", "Kitten", "cutecats", "catsnamedafterfood")
        val random = Random
        val posts = getPosts(subreddits[random.nextInt(subreddits.size)], event.monke).filter { it.isMedia() }
        val language = event.getLanguage()

        if (posts.isEmpty()) {
            event.reply {
                type(CommandReply.Type.EXCEPTION)
                title(
                    TranslationHandler.getString(
                        language = language,
                        key = "command_error.corrupt_web_data",
                        values = arrayOf("Reddit")
                    )
                )
                footer()
                send()
            }
            return
        }

        checkAndSendPost(event, posts[random.nextInt(posts.size)])
    }
}