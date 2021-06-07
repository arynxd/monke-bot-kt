package me.arynxd.monke.commands.`fun`

import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.util.checkAndSendPost
import me.arynxd.monke.util.getPosts
import kotlin.random.Random

@Suppress("UNUSED")
class CatCommand : Command(
    CommandMetaData(
        name = "cat",
        description = "Shows cute cats from Reddit.",
        category = CommandCategory.FUN,
        aliases = listOf("kitty"),
        flags = listOf(CommandFlag.SUSPENDING),
        cooldown = 3000L
    )
) {
    override suspend fun runSuspend(event: CommandEvent) {
        val subreddits = listOf("Kitten", "cutecats", "catsnamedafterfood")
        val random = Random
        val posts = getPosts(subreddits[random.nextInt(subreddits.size)], event.monke)
            .filter { it.isMedia() }
        val language = event.language

        if (posts.isEmpty()) {
            event.reply {
                type(CommandReply.Type.EXCEPTION)
                title(
                    translate {
                        lang = language
                        path = "command_error.corrupt_web_data"
                        values = arrayOf("Reddit")
                    }
                )
                footer()
                event.thread.post(this)
            }
            return
        }

        checkAndSendPost(event, posts[random.nextInt(posts.size)])
    }
}