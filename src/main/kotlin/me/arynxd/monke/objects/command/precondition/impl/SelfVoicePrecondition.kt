package me.arynxd.monke.objects.command.precondition.impl

import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.precondition.Precondition
import me.arynxd.monke.objects.command.threads.CommandReply

class SelfVoicePrecondition : Precondition {
    override fun pass(event: CommandEvent): Boolean {
        return event.selfMember.voiceState?.channel != null
    }

    override fun onFail(event: CommandEvent) {
        event.replyAsync {
            type(CommandReply.Type.EXCEPTION)
            title("I am not in a voice channel.")
            footer()
            event.thread.post(this)
        }
    }
}