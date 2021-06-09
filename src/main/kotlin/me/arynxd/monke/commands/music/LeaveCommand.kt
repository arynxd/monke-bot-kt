package me.arynxd.monke.commands.music

import me.arynxd.monke.handlers.MusicHandler
import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandMetaData
import me.arynxd.monke.objects.command.threads.CommandReply

@Suppress("UNUSED")
class LeaveCommand : Command(
    CommandMetaData(
        name = "leave",
        description = "Leaves the voice channel, if it's in one.",
        category = CommandCategory.MUSIC,

        finalCheck = { it.member.voiceState?.channel != null && it.selfMember.voiceState?.channel != null },
        finalCheckFail = {
            it.replyAsync {
                type(CommandReply.Type.EXCEPTION)
                title("You or I are not in a voice channel.")
                footer()
                send()
            }
        }
    )
) {
    override fun runSync(event: CommandEvent) {
        val audioManager = event.guild.audioManager
        val musicHandler = event.monke.handlers[MusicHandler::class]
        musicHandler.leaveChannel(event.guild)

        event.replyAsync {
            type(CommandReply.Type.SUCCESS)
            title(
                translate {
                    lang = event.language
                    path = "music.channel_left"
                    values = arrayOf(
                        audioManager.connectedChannel?.name ?: throw IllegalStateException("Voice channel not present")
                    )
                }
            )
            footer()
            event.thread.post(this)
        }
    }
}