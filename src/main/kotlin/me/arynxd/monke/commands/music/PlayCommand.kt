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
        val musicHandler = event.monke.handlers.get(MusicHandler::class)
        val musicManager = musicHandler.getGuildMusicManager(event.guild)
        val channel = event.channel
        val voiceChannel = event.member.voiceState!!.channel!!

        val query = event.getVararg<String>(0).joinToString(" ").let {
            if (it.isValidUrl()) it
            else "ytsearch:$it"
        }

        musicHandler.playerManager.loadItemOrdered(musicManager, query, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                channel.sendMessage(
                    TranslationHandler.getString(event.getLanguage(), "music.adding_to_queue", track.info.title)
                ).queue()

                musicManager.play(track, voiceChannel)
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                val firstTrack: AudioTrack = playlist.selectedTrack ?: playlist.tracks[0]

                channel.sendMessage(
                    TranslationHandler.getString(event.getLanguage(), "music.adding_to_queue", firstTrack.info.title) +
                            TranslationHandler.getString(event.getLanguage(), "music.first_track_of", playlist.name)
                ).queue()

                musicManager.play(firstTrack, voiceChannel)
            }

            override fun noMatches() {
                channel.sendMessage(TranslationHandler.getString(event.getLanguage(), "music.nothing_found", query))
                    .queue()
            }

            override fun loadFailed(exception: FriendlyException) {
                channel.sendMessage(
                    TranslationHandler.getString(
                        event.getLanguage(),
                        "music.could_not_play",
                        exception.message.toString()
                    )
                ).queue()
            }
        })
    }
}