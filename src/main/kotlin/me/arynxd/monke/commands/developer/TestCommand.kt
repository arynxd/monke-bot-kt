package me.arynxd.monke.commands.developer

import dev.minn.jda.ktx.await
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.exception.TestException
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

@Suppress("UNUSED")
class TestCommand : Command(
    name = "test",
    description = "Tests the bot's basic functionality.",
    category = CommandCategory.DEVELOPER,
    flags = listOf(CommandFlag.DEVELOPER_ONLY, CommandFlag.ASYNC),

    ) {

    override suspend fun runSuspend(event: CommandEvent) {
        val language = event.getLanguage()

        event.reply {
            type(CommandReply.Type.SUCCESS)
            title(
                TranslationHandler.getString(
                    language = language,
                    key = "command.test.keyword.success"
                )
            )
            footer()
            send()
        }

        event.reply {
            type(CommandReply.Type.EXCEPTION)
            title(
                TranslationHandler.getString(
                    language = language,
                    key = "command.test.keyword.error"
                )
            )
            footer()
            send()
        }

        event.reply {
            type(CommandReply.Type.INFORMATION)
            title(
                TranslationHandler.getString(
                    language = language,
                    key = "command.test.keyword.embed"
                )
            )
            description(
                TranslationHandler.getString(
                    language = language,
                    key = "command.test.keyword.event_waiting"
                )
            )
            footer()
            send()
        }
        try {
            withTimeout(7000) {
                val messageEvent = event.monke.jda.await<GuildMessageReceivedEvent> { guildEvent ->
                    guildEvent.author == event.user && guildEvent.channel == event.channel
                }

                event.reply {
                    type(CommandReply.Type.SUCCESS)
                    title(
                        TranslationHandler.getString(
                            language = language,
                            key = "command.test.keyword.captured_result",
                            values = arrayOf(messageEvent.message.contentRaw)
                        )
                    )
                    footer()
                    send()
                }
            }
        }
        catch (exception: TimeoutCancellationException) {
            event.reply {
                type(CommandReply.Type.EXCEPTION)
                title(
                    TranslationHandler.getString(
                        language = language,
                        key = "command.test.keyword.time_out"
                    )
                )
                footer()
                send()
            }
        }

        throw TestException("Thrown from test command!")
    }
}