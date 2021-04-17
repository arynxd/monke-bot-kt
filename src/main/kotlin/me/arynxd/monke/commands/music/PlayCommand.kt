package me.arynxd.monke.commands.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.arynxd.monke.handlers.MusicHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.events.types.CommandEvent
import me.arynxd.monke.util.isValidUrl

@Suppress("UNUSED")
class PlayCommand : Command(
    CommandMetaData(
        name = "play",
        description = "Plays music from youtube or soundcloud",
        category = CommandCategory.MUSIC,
        flags = listOf(CommandFlag.SUSPENDING),
        arguments = ArgumentConfiguration(
            listOf(
                ArgumentString(
                    name = "song",
                    description = "The track to play.",
                    required = true,
                    type = ArgumentType.VARARG
                )
            )
        ),

        finalCheck = { it.member.voiceState?.channel != null },
        finalCheckFail = {
            it.replyAsync {
                type(CommandReply.Type.EXCEPTION)
                title("You are not in a voice channel.")
                footer()
                send()
            }
        }
    )
) {
    override suspend fun runSuspend(event: CommandEvent) {
        val channel = event.channel
        val voiceChannel = event.member.voiceState!!.channel!!
        val musicHandler = event.monke.handlers.get(MusicHandler::class)
        val musicManager = musicHandler.getGuildMusicManager(event.guild, channel, voiceChannel)

        if (musicManager.channel != channel) {
            event.reply {
                type(CommandReply.Type.EXCEPTION)
                title("I'm locked to ${musicManager.channel.asMention} for this session.")
                footer()
                send()
            }
            return
        }


        if (musicManager.voiceChannel != voiceChannel) {
            event.reply {
                type(CommandReply.Type.EXCEPTION)
                title("Join ${musicManager.voiceChannel.name} to use my music commands.")
                footer()
                send()
            }
            return
        }


        val query = event.getVararg<String>(0).joinToString(" ").let {
            if (it.isValidUrl()) it
            else "ytsearch:$it"
        }

        musicHandler.playerManager.loadItemOrdered(musicManager, query, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                musicManager.play(track, voiceChannel)
                val message =
                    if (musicManager.hasNext())
                        "Added ${track.info.title} to the queue"
                    else
                        "Now playing ${track.info.title}"


                event.replyAsync {
                    type(CommandReply.Type.SUCCESS)
                    title(message)
                    footer()
                    send()
                }
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                val firstTrack: AudioTrack = playlist.selectedTrack ?: playlist.tracks[0]

                val message =
                    if (musicManager.hasNext())
                        "Added ${firstTrack.info.title} to the queue"
                    else
                        "Now playing ${firstTrack.info.title}"


                event.replyAsync {
                    type(CommandReply.Type.SUCCESS)
                    title(message)
                    footer()
                    send()
                }
            }

            override fun noMatches() {
                event.replyAsync {
                    type(CommandReply.Type.EXCEPTION)
                    title("No matches were found for `$query`")
                    footer()
                    send()
                }
            }

            override fun loadFailed(exception: FriendlyException) {
                event.replyAsync {
                    type(CommandReply.Type.EXCEPTION)
                    title("Something went wrong when loading that track, ${exception.cause}")
                    footer()
                    send()
                }
            }
        })
    }
}