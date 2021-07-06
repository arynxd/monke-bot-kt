package me.arynxd.monke.commands.moderation

import me.arynxd.monke.handlers.translation.translate
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.util.awaitConfirmation
import me.arynxd.monke.util.purge
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel

@Suppress("UNUSED")
class PurgeCommand : Command(
    CommandMetaData(
        name = "purge",
        description = "Purges all messages from this channel",
        category = CommandCategory.MODERATION,
        flags = listOf(CommandFlag.SUSPENDING),
        cooldown = 10_000L,
        memberPermissions = listOf(Permission.MANAGE_CHANNEL),
        botPermissions = listOf(Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE)
    ),
) {
    override suspend fun runSuspend(event: CommandEvent) {
        val language = event.language
        val resp = event.reply {
            type(CommandReply.Type.EXCEPTION)
            title(
                translate {
                    lang = language
                    path = "command.purge.response.confirmation"
                }
            )
            footer()
        }

        val message = event.thread.awaitPost(resp)
        val confirmation = awaitConfirmation(message, event.user, event.monke)

        if (confirmation.isError) {
            val err = confirmation.error

            if (err == "timeout") {
                event.reply {
                    type(CommandReply.Type.EXCEPTION)
                    title(
                        translate {
                            lang = language
                            path = "command_error.timeout"
                        }
                    )
                    footer()
                    event.thread.post(this)
                }
            }
            return
        }

        if (confirmation.data) {
            event.channel.purge()
        }
        else {
            event.reply {
                type(CommandReply.Type.SUCCESS)
                title(
                    translate {
                        lang = language
                        path = "command.purge.response.aborted"
                    }
                )
                footer()
                event.thread.post(this)
            }
            return
        }
    }
}