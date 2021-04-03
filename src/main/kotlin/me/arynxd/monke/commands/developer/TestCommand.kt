package me.arynxd.monke.commands.developer

import dev.minn.jda.ktx.await
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandFlag
import me.arynxd.monke.objects.exception.TestException
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

@Suppress("UNUSED")
class TestCommand : Command(
    name = "test",
    description = "Tests the bot's basic functionality.",
    category = CommandCategory.DEVELOPER,
    flags = listOf(CommandFlag.DEVELOPER_ONLY),

    ) {

    override suspend fun run(event: CommandEvent) {
        val language = event.getLanguage()

        event.reply {
            success()
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
            exception()
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
            information()
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
                    success()
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
        } catch (exception: TimeoutCancellationException) {
            event.reply {
                exception()
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