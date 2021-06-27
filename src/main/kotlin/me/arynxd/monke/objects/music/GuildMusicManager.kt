package me.arynxd.monke.objects.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.ref
import me.arynxd.monke.Monke
import me.arynxd.monke.handlers.CommandThreadHandler
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.objects.command.threads.CommandThread
import me.arynxd.monke.util.MIN_PERMISSIONS
import me.arynxd.monke.util.hasMinimumPermissions
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.*

class GuildMusicManager(
    manager: AudioPlayerManager,
    guild: Guild,
    channel: TextChannel,
    voiceChannel: VoiceChannel,

    user: User,
    val monke: Monke,
    val sourceId: Long
) {
    val player = manager.createPlayer()
    val scheduler = TrackScheduler(player).also { player.addListener(it) }
    val audioManager = guild.audioManager

    private var messageId = -1L

    val textChannel by channel.ref()
    val voiceChannel by voiceChannel.ref()
    val user by user.ref()

    val sendHandler: AudioSendHandler
        get() = AudioPlayerSendHandler(player)

    fun play(track: AudioTrack, channel: VoiceChannel) {
        if (!textChannel.hasMinimumPermissions()) {
            textChannel
                .sendMessage("Oops! I'm missing some permissions I need to function (${MIN_PERMISSIONS.joinToString { it.name }})")
                .referenceById(sourceId).queue()
            return
        }

        audioManager.openAudioConnection(channel)
        scheduler.queue(track)

        if (messageId == -1L) {
            sendThread {
                updateThread(it, track)
            }
            return
        }
        updateThread(getThread(), scheduler.peek())
    }

    private fun getThread(): CommandThread {
        if (messageId == -1L) {
            throw IllegalStateException("Message ID was not set (this should never happen)")
        }

        return monke.handlers[CommandThreadHandler::class].getOrNew(messageId)
    }

    private fun updateThread(thread: CommandThread, nowPlaying: AudioTrack?) {
        val upNext = scheduler.peek()?.info?.title ?: "---"
        val duration = nowPlaying?.duration ?: "---"
        val np = nowPlaying?.info?.title ?: "---"

        getReply().apply {
            type(CommandReply.Type.SUCCESS)
            title("Music")
            field("Now Playing", np, true)
            field("Duration", duration, true)
            field("Up Next", upNext, true)
            thread.post(this)
        }
    }

    private fun getReply(): CommandReply {
        return CommandReply(
            monke = monke,
            channel = textChannel,
            user = null,
            messageId = messageId
        )
    }

    private fun sendThread(callback: (CommandThread) -> Unit) {
        val embed = Embed(
            title = "Music",
            fields = listOf(
                MessageEmbed.Field("Now Playing", "---", true),
                MessageEmbed.Field("Duration", "---", true),
                MessageEmbed.Field("Up Next", "---", true)
            )
        )
        textChannel.sendMessageEmbeds(embed).queue {
            this.messageId = it.idLong
            val thr = CommandThread(messageId, listOf(messageId))
            monke.handlers[CommandThreadHandler::class].put(thr)
            callback(thr)
        }
    }

    fun hasNext() = scheduler.hasNext()
}