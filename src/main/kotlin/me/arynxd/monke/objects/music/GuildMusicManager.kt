package me.arynxd.monke.objects.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.minn.jda.ktx.ref
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel

class GuildMusicManager(
    manager: AudioPlayerManager,
    guild: Guild,
    channel: TextChannel,
    voiceChannel: VoiceChannel
) {
    val player: AudioPlayer = manager.createPlayer()
    val scheduler: TrackScheduler = TrackScheduler(player).also { player.addListener(it) }
    val audioManager = guild.audioManager

    val channel: TextChannel by channel.ref()
    val voiceChannel: VoiceChannel by voiceChannel.ref()

    fun getSendHandler(): AudioPlayerSendHandler = AudioPlayerSendHandler(player)

    fun play(track: AudioTrack, channel: VoiceChannel) {
        audioManager.openAudioConnection(channel)
        scheduler.queue(track)
    }

    fun hasNext(): Boolean {
        return scheduler.hasNext()
    }

    fun sendControlEmbed() {
        TODO("Unimplemented")
    }

    fun handleReaction(reaction: String) {
        TODO("Unimplemented")
    }

    fun changeNowPlaying(track: AudioTrack) {
        TODO("Unimplemented")
    }
}