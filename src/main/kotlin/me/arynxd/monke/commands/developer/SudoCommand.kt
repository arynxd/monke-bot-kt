package me.arynxd.monke.commands.developer

import kotlinx.coroutines.delay
import me.arynxd.monke.events.CommandPreprocessEvent
import me.arynxd.monke.handlers.CommandHandler
import me.arynxd.monke.handlers.translation.translate
import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.types.ArgumentCommand
import me.arynxd.monke.objects.argument.types.ArgumentMember
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.command.threads.CommandReply
import net.dv8tion.jda.api.entities.Member

@Suppress("UNUSED")
class SudoCommand : Command(
    CommandMetaData(
        name = "sudo",
        description = "Allows you to execute commands as if you were another user",
        category = CommandCategory.DEVELOPER,
        flags = listOf(CommandFlag.DEVELOPER_ONLY, CommandFlag.SUSPENDING),
        arguments = ArgumentConfiguration(
            ArgumentMember(
                name = "member",
                description = "The member to impersonate",
                required = true,
                type = Argument.Type.REGULAR
            ),
            ArgumentCommand(
                name = "command",
                description = "The command to execute",
                required = true,
                type = Argument.Type.REGULAR
            ),
            ArgumentString(
                name = "args",
                description = "The arguments to execute the command with",
                required = false,
                type = Argument.Type.VARARG
            )
        )
    )
) {
    override suspend fun runSuspend(event: CommandEvent) {
        val target = event.argument<Member>(0)
        val command = event.argument<Command>(1)
        val args = if (event.isArgumentPresent(2)) event.vararg<String>(2) else emptyList()
        val monke = event.monke
        val language = event.language

        val preprocessEvent = CommandPreprocessEvent(
            monke = monke,
            message = event.message,
            guild = target.guild,
            user = target.user,
            member = target,
            jda = target.jda,
            channel = event.channel
        )

        val commandEvent = CommandEvent(monke, args.toMutableList(), command, preprocessEvent)

        event.replyAsync {
            type(CommandReply.Type.INFORMATION)
            title(
                translate {
                    lang = language
                    path = "command.sudo.response.start"
                    values = arrayOf(command.getName(language), target.user.asTag)
                }
            )
            footer()
            event.thread.post(this)
        }
        delay(1_500) //Give some delay to the execution

        event.monke.handlers[CommandHandler::class].launchCommand(command, commandEvent)
    }
}