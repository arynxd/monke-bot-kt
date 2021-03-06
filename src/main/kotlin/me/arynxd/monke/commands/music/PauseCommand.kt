package me.arynxd.monke.commands.music

import me.arynxd.monke.handlers.MusicHandler
import me.arynxd.monke.objects.command.*

@Suppress("UNUSED")
class PauseCommand : Command(
    name = "pause",
    description = "Starts and stops the player.",
    category = CommandCategory.MUSIC,
    flags = listOf(CommandFlag.ASYNC),

    finalCheck = { it.member.voiceState?.channel != null },
    finalCheckFail = {
        it.replyAsync {
            type(CommandReply.Type.EXCEPTION)
            title("You or I are not in a voice channel.")
            footer()
            send()
        }
    }
) {
    override suspend fun runSuspend(event: CommandEvent) {
        val musicManager = event.monke.handlers.get(MusicHandler::class).getGuildMusicManager(
            guild = event.guild,
            channel = event.channel,
            voiceChannel = event.member.voiceState!!.channel!!
        )

        if (musicManager.player.isPaused) {
            event.reply {
                type(CommandReply.Type.SUCCESS)
                title("Un-paused the player")
                footer()
                send()
            }
            musicManager.player.isPaused = false
            return
        }

        event.reply {
            type(CommandReply.Type.SUCCESS)
            title("Paused the player")
            footer()
            send()
        }
        musicManager.player.isPaused = true
    }
}