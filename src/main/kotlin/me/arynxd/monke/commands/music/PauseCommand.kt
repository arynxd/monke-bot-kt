package me.arynxd.monke.commands.music

import me.arynxd.monke.handlers.MusicHandler
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.command.precondition.impl.MemberVoicePrecondition
import me.arynxd.monke.objects.command.precondition.impl.SelfVoicePrecondition
import me.arynxd.monke.objects.command.threads.CommandReply

@Suppress("UNUSED")
class PauseCommand : Command(
    CommandMetaData(
        name = "pause",
        description = "Starts and stops the player.",
        category = CommandCategory.MUSIC,
        flags = listOf(CommandFlag.SUSPENDING),

        preconditions = listOf(MemberVoicePrecondition(), SelfVoicePrecondition())
    )
) {
    override suspend fun runSuspend(event: CommandEvent) {
        val musicManager = event.monke.handlers[MusicHandler::class].getGuildMusicManager(
            guild = event.guild,
            channel = event.channel,
            voiceChannel = event.member.voiceState!!.channel!!,
            user = event.user,
            sourceId = event.messageIdLong
        )

        if (musicManager.player.isPaused) {
            event.reply {
                type(CommandReply.Type.SUCCESS)
                title("Un-paused the player")
                footer()
                event.thread.post(this)
            }
            musicManager.player.isPaused = false
            return
        }

        event.reply {
            type(CommandReply.Type.SUCCESS)
            title("Paused the player")
            footer()
            event.thread.post(this)
        }
        musicManager.player.isPaused = true
    }
}