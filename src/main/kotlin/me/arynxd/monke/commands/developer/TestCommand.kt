package me.arynxd.monke.commands.developer

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentBoolean
import me.arynxd.monke.objects.argument.types.ArgumentInt
import me.arynxd.monke.objects.argument.types.ArgumentLong
import me.arynxd.monke.objects.argument.types.ArgumentMember
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandFlag
import me.arynxd.monke.util.sendError
import me.arynxd.monke.util.sendSuccess
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

@Suppress("UNUSED")
class TestCommand : Command(
    name = "test",
    description = "Tests the bot's basic functionality.",
    category = CommandCategory.DEVELOPER,
    flags = listOf(CommandFlag.DEVELOPER_ONLY),

    arguments = ArgumentConfiguration(
        listOf(
            ArgumentBoolean(
                name = "boolean-test",
                description = "Test the boolean arg type. Controls if the other tests run.",
                required = false,
                type = ArgumentType.REGULAR,
            ),

            ArgumentMember(
                name = "member-test",
                description = "Test the member arg type. Must not be a bot.",
                required = false,
                type = ArgumentType.REGULAR,
                condition = { !it.user.isBot }
            ),

            ArgumentLong(
                name = "long-test",
                description = "Test the long arg type. Must be less than 0.",
                required = false,
                type = ArgumentType.REGULAR,
                condition = { it < 0 }
            ),

            ArgumentInt(
                name = "int-test",
                description = "Test the int type. Must be greater than 0.",
                required = false,
                type = ArgumentType.REGULAR,
                condition = { it > 0 },
            )
        )
    ),

    ) {

    override suspend fun run(event: CommandEvent) {
        val language = event.getLanguage()

        val success = TranslationHandler.getString(language, "command.test.keyword.success")
        val error = TranslationHandler.getString(language, "command.test.keyword.error")
        val embed = TranslationHandler.getString(language, "command.test.keyword.embed")
        val eventWaiting = TranslationHandler.getString(language, "command.test.keyword.event_waiting")
        val timeOut = TranslationHandler.getString(language, "command.test.keyword.time_out")

        sendSuccess(event.message, success)
        sendError(event.message, error)

        event.sendEmbed(
            Embed(
                title = embed,
                description = eventWaiting
            )
        )

        try {
            withTimeout(7000) {
                val messageEvent = event.monke.jda.await<GuildMessageReceivedEvent> { guildEvent ->
                    guildEvent.author == event.user
                            && guildEvent.channel == event.channel
                }
                val capturedResult = TranslationHandler.getString(
                    language,
                    "command.test.keyword.captured_result",
                    messageEvent.message.contentRaw
                )
                event.sendEmbed(
                    Embed(
                        description = capturedResult
                    )
                )
            }
        } catch (exception: TimeoutCancellationException) {
            sendError(event.message, timeOut)
        }

        if (event.isArgumentPresent(0)) {
            val boolean = event.getArgument<Boolean>(0)
            if (boolean) {
                sendSuccess(event.message, "Boolean ${event.getArgument<Boolean>(0)}")
            }
            else {
                sendError(event.message, "Boolean ${event.getArgument<Boolean>(0)}")
            }

            if (event.isArgumentPresent(1)) {
                sendSuccess(event.message, "Member ${event.getArgument<Member>(1).effectiveName}")
            }

            if (event.isArgumentPresent(2)) {
                sendSuccess(event.message, "Long ${event.getArgument<Long>(2)}")
            }

            if (event.isArgumentPresent(3)) {
                sendSuccess(event.message, "Int ${event.getArgument<Int>(3)}")
            }
        }
    }
}