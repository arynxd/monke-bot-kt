package me.arynxd.monke.commands.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.arynxd.monke.handlers.MusicHandler
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.util.isValidUrl
import me.arynxd.monke.util.sendError
import me.arynxd.monke.util.sendSuccess

@Suppress("UNUSED")
class PlayCommand : Command(
    name = "play",
    description = "Plays music from youtube or soundcloud",
    category = CommandCategory.MUSIC,
    arguments = ArgumentConfiguration(
        listOf(
            ArgumentString(
                name = "Track",
                description = "The track to play.",
                required = true,
                type = ArgumentType.VARARG
            )
        )
    ),

    finalCheck = { it.member.voiceState?.channel != null },
    finalCheckFail = { sendError(it.message, "You are not in a voice channel.") }
) {
    override suspend fun run(event: CommandEvent) {

        val channel = event.channel
        val message = event.message
        val voiceChannel = event.member.voiceState!!.channel!!
        val musicHandler = event.monke.handlers.get(MusicHandler::class)
        val musicManager = musicHandler.getGuildMusicManager(event.guild, channel, voiceChannel)


        if (musicManager.channel != channel) {
            sendError(event.message, "I'm locked to ${musicManager.channel.asMention} for this session.")
            return
        }


        if (musicManager.voiceChannel != voiceChannel) {
            sendError(event.message, "Join ${musicManager.voiceChannel.name} to use my music commands.")
            return
        }


        val query = event.getVararg<String>(0).joinToString(" ").let {
            if (it.isValidUrl()) it
            else "ytsearch:$it"
        }

        musicHandler.playerManager.loadItemOrdered(musicManager, query, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                musicManager.play(track, voiceChannel)

                if (musicManager.hasNext()) {
                    sendSuccess(message, "Added ${track.info.title} to the queue")
                    return
                }

                sendSuccess(message, "Now playing ${track.info.title}")
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                val firstTrack: AudioTrack = playlist.selectedTrack ?: playlist.tracks[0]
                musicManager.play(firstTrack, voiceChannel)
                if (musicManager.hasNext()) {

                    sendSuccess(message, "Added ${firstTrack.info.title} to the queue")
                    return
                }

                sendSuccess(message, "Now playing ${firstTrack.info.title}")
            }

            override fun noMatches() {
                sendError(message, "No matches were found for `$query`")
            }

            override fun loadFailed(exception: FriendlyException) {
                sendError(message, "Something went wrong when loading that track, ${exception.cause}")
            }
        })
    }
}