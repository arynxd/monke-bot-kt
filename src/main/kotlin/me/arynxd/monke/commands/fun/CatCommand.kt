package me.arynxd.monke.commands.`fun`

import me.arynxd.monke.handlers.translation.translate
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandMetaData
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
        cooldown = 3000L
    )
) {
    override fun runSync(event: CommandEvent) {
        val subreddits = listOf("Kitten", "cutecats", "catsnamedafterfood")
        val random = Random
        val language = event.language

        getPosts(subreddits[random.nextInt(subreddits.size)], event.monke)
            .filter { it.isMedia() }
            .doOnError {
                event.replyAsync {
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
            }
            .collectList()
            .subscribe { checkAndSendPost(event, it.random()) }
    }
}