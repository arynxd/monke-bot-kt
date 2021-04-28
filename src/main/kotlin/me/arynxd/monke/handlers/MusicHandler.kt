package me.arynxd.monke.handlers

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.arynxd.monke.Monke
import me.arynxd.monke.objects.handlers.Handler
import me.arynxd.monke.objects.music.GuildMusicManager
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent

class MusicHandler(
    override val monke: Monke
) : Handler() {
    val playerManager = DefaultAudioPlayerManager()
    private val musicManagers = HashMap<Long, GuildMusicManager>()
    private val lock = Mutex()

    override fun onEnable() {
        AudioSourceManagers.registerLocalSource(playerManager)
        AudioSourceManagers.registerRemoteSources(playerManager)
    }

    suspend fun getGuildMusicManager(
        guild: Guild,
        channel: TextChannel,
        voiceChannel: VoiceChannel
    ): GuildMusicManager {

        val guildId = guild.idLong
        return lock.withLock {
            val musicManager =
                musicManagers[guildId] ?: GuildMusicManager(
                    channel = channel,
                    voiceChannel = voiceChannel,
                    guild = guild,
                    manager = playerManager
                ).also {
                    musicManagers[guildId] = it
                }

            guild.audioManager.sendingHandler = musicManager.getSendHandler()

            return@withLock musicManager
        }
    }

    fun leaveChannel(channel: VoiceChannel) {
        val audioManager = channel.guild.audioManager

        audioManager.closeAudioConnection()
        musicManagers.remove(channel.guild.idLong)
    }

    fun leaveChannel(guild: Guild) {
        val audioManager = guild.audioManager

        audioManager.closeAudioConnection()
        musicManagers.remove(guild.idLong)
    }

    override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        val audioManager = event.guild.audioManager

        if (leaveIfAlone(event.guild.idLong, audioManager.connectedChannel)) {
            musicManagers.remove(event.guild.idLong)
        }
    }

    override fun onGuildVoiceMove(event: GuildVoiceMoveEvent) {
        val audioManager = event.guild.audioManager
        val id = event.guild.idLong

        if (!leaveIfAlone(id, audioManager.connectedChannel)) {
            val musicManager = musicManagers[id] ?: return

            musicManagers[id] = GuildMusicManager(
                channel = musicManager.channel,
                voiceChannel = event.channelJoined,
                guild = event.guild,
                manager = playerManager
            )
        }
    }

    private fun leaveIfAlone(guildId: Long, channel: VoiceChannel?): Boolean {
        if (!musicManagers.containsKey(guildId) || channel == null)
            return true

        if (channel.members.count { !it.user.isBot } == 0) { // No humans
            leaveChannel(channel)
            return true
        }

        return false
    }
}