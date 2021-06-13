package me.arynxd.monke.objects.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class TrackScheduler(
    private val player: AudioPlayer
) : AudioEventAdapter() {
    private val queue = LinkedList<AudioTrack>()

    fun queue(track: AudioTrack) {
        if (!player.startTrack(track, true)) queue.offer(track)
    }

    fun nextTrack(): AudioTrack? {
        val track = peek()
        player.startTrack(track, false)
        return track
    }

    fun peek(): AudioTrack? = queue.peek()

    fun hasNext() = peek() != null

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            nextTrack()
        }
    }
}