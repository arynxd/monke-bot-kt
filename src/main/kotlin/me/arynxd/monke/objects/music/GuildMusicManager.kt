package me.arynxd.monke.objects.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager

class GuildMusicManager(
    manager: AudioPlayerManager,
) {
    val player: AudioPlayer = manager.createPlayer()
    val scheduler: TrackScheduler = TrackScheduler(player).also { player.addListener(it) }

    fun getSendHandler(): AudioPlayerSendHandler = AudioPlayerSendHandler(player)
}