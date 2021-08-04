package me.arynxd.monke.objects.command.precondition.impl

import me.arynxd.monke.handlers.translation.translate
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.precondition.Precondition
import me.arynxd.monke.objects.command.threads.CommandReply

class DeveloperPrecondition : Precondition {
    override fun pass(event: CommandEvent): Boolean {
        return event.isDeveloper
    }

    override fun onFail(event: CommandEvent) {
        event.replyAsync {
            type(CommandReply.Type.EXCEPTION)
            title(
                translate {
                    lang = event.language
                    path = "command_error.developer_only"
                }
            )
            footer()
            event.thread.post(this)
        }
    }
}