package me.arynxd.monke.commands.`fun`

import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.util.checkAndSendPost
import me.arynxd.monke.util.getPosts
import me.arynxd.monke.util.sendError
import kotlin.random.Random

@Suppress("UNUSED")
class CatCommand : Command(
    name = "cat",
    description = "Shows cute cats from Reddit.",
    category = CommandCategory.FUN,
    aliases = listOf("kitty"),
    cooldown = 3000L,

    ) {

    override suspend fun run(event: CommandEvent) {
        val subreddits = listOf("kittens", "Kitten", "cutecats", "catsnamedafterfood")
        val random = Random
        val posts = getPosts(subreddits[random.nextInt(subreddits.size)], event.monke).filter { it.isMedia() }
        val language = event.getLanguage()

        if (posts.isEmpty()) {
            event.reply {
                exception()
                title(
                    TranslationHandler.getString(
                        language = language,
                        key = "command_error.corrupt_web_data",
                        values = arrayOf("Reddit")
                    )
                )
                footerIcon()
                send()
            }
            return
        }

        checkAndSendPost(event, posts[random.nextInt(posts.size)])
    }
}