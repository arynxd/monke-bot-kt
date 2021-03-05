package me.arynxd.monke.commands.music

import me.arynxd.monke.handlers.MusicHandler
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.translation.Language
import me.arynxd.monke.util.sendError
import me.arynxd.monke.util.sendSuccess

class LeaveCommand : Command(
    name = "leave",
    description = "Leaves the voice channel, if it's in one.",
    category = CommandCategory.MUSIC
) {
    override suspend fun run(event: CommandEvent) {
        val audioManager = event.guild.audioManager
        if(audioManager.isConnected) {
            val musicHandler = event.monke.handlers.get(MusicHandler::class.java)
            musicHandler.leaveChannel(event.guild)

            sendSuccess(event.message, TranslationHandler.getString(event.getLanguage(), "music.channel_left", audioManager.connectedChannel!!.name))
        } else {
            sendError(event.message, TranslationHandler.getString(event.getLanguage(), "music.not_in_channel"))
        }
    }
}