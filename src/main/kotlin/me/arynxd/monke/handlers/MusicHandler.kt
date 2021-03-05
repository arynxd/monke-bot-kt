package me.arynxd.monke.handlers

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.music.GuildMusicManager
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import net.dv8tion.jda.api.managers.AudioManager

class MusicHandler constructor(
    override val monke: Monke
) : Handler() {
    val playerManager = DefaultAudioPlayerManager()
    private val musicManagers = HashMap<Long, GuildMusicManager>()

    override fun onEnable() {
        AudioSourceManagers.registerLocalSource(playerManager);
        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    fun getGuildAudioPlayer(guild: Guild): GuildMusicManager {
        val guildId: Long = guild.idLong
        val musicManager: GuildMusicManager = musicManagers[guildId] ?: GuildMusicManager(playerManager).also { musicManagers[guildId] = it }

        guild.audioManager.sendingHandler = musicManager.getSendHandler()

        return musicManager
    }

    fun play(guild: Guild, musicManager: GuildMusicManager, track: AudioTrack) {
        connectToFirstVoiceChannel(guild.audioManager)
        musicManager.scheduler.queue(track)
    }

    fun skipTrack(channel: TextChannel) {
        val musicManager = getGuildAudioPlayer(channel.guild)

        musicManager.scheduler.nextTrack()
        channel.sendMessage("Skipped to next track.").queue()
    }

    private fun connectToFirstVoiceChannel(audioManager: AudioManager) {
        if (!audioManager.isConnected) {
            audioManager.openAudioConnection(audioManager.guild.voiceChannels.first())
        }
    }

    fun leaveChannel(channel: VoiceChannel) {
        val audioManager = channel.guild.audioManager

        audioManager.sendingHandler = null
        audioManager.closeAudioConnection()
        musicManagers.remove(channel.guild.idLong)
    }
    fun leaveChannel(guild: Guild) {
        val audioManager = guild.audioManager

        audioManager.sendingHandler = null
        audioManager.closeAudioConnection()
        musicManagers.remove(guild.idLong)
    }

    override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        val audioManager = event.guild.audioManager

        checkIsAlone(event.guild.idLong, audioManager.connectedChannel)
    }

    override fun onGuildVoiceMove(event: GuildVoiceMoveEvent) {
        val audioManager = event.guild.audioManager

        checkIsAlone(event.guild.idLong, audioManager.connectedChannel)
    }

    private fun checkIsAlone(guildId: Long, channel: VoiceChannel?) {
        if(!musicManagers.containsKey(guildId) || channel == null) return

        if(channel.members.size == 1) {
            leaveChannel(channel)
        }
    }
}