package me.arynxd.monke.commands.music

import me.arynxd.monke.handlers.MusicHandler
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent

@Suppress("UNUSED")
class LeaveCommand : Command(
    name = "leave",
    description = "Leaves the voice channel, if it's in one.",
    category = CommandCategory.MUSIC,

    finalCheck = { it.member.voiceState?.channel != null && it.selfMember.voiceState?.channel != null },
    finalCheckFail = {
        it.replyAsync {
            exception()
            title("You or I are not in a voice channel.")
            footer()
            send()
        }
    }
) {
    override suspend fun run(event: CommandEvent) {
        val audioManager = event.guild.audioManager
        val musicHandler = event.monke.handlers.get(MusicHandler::class)
        musicHandler.leaveChannel(event.guild)

        event.reply {
            success()
            title(
                TranslationHandler.getString(
                    language = event.getLanguage(),
                    key = "music.channel_left",
                    values = arrayOf(
                        audioManager.connectedChannel?.name ?: throw IllegalStateException("Voice channel not present")
                    )
                )
            )
            footer()
            send()
        }
    }
}