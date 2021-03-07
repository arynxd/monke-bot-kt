package me.arynxd.monke.commands.music

import me.arynxd.monke.handlers.MusicHandler
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.util.sendError
import me.arynxd.monke.util.sendSuccess

@Suppress("UNUSED")
class PauseCommand : Command(
    name = "pause",
    description = "Starts and stops the player.",
    category = CommandCategory.MUSIC,

    finalCheck = { it.member.voiceState?.channel != null },
    finalCheckFail = { sendError(it.message, "You are not in a voice channel.") }
) {
    override suspend fun run(event: CommandEvent) {
        val musicManager = event.monke.handlers.get(MusicHandler::class).getGuildMusicManager(
            guild = event.guild,
            channel = event.channel,
            voiceChannel = event.member.voiceState!!.channel!!
        )

        val message = event.message

        if (musicManager.player.isPaused) {
            sendSuccess(message, "Un-paused the player")
            musicManager.player.isPaused = false
            return
        }

        sendSuccess(message, "Paused the player")
        musicManager.player.isPaused = true
    }
}