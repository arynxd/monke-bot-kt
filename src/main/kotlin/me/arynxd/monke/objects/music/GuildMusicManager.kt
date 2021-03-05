package me.arynxd.monke.objects.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.VoiceChannel

class GuildMusicManager(
    manager: AudioPlayerManager,
    guild: Guild
) {
    val player: AudioPlayer = manager.createPlayer()
    val scheduler: TrackScheduler = TrackScheduler(player).also { player.addListener(it) }
    val audioManager = guild.audioManager

    fun getSendHandler(): AudioPlayerSendHandler = AudioPlayerSendHandler(player)

    fun play(track: AudioTrack, channel: VoiceChannel) {
        audioManager.openAudioConnection(channel)
        scheduler.queue(track)
    }
}