package me.arynxd.monke.objects.command.precondition

import me.arynxd.monke.objects.command.CommandEvent

interface Precondition {
    fun pass(event: CommandEvent): Boolean

    fun onFail(event: CommandEvent)
    fun onSuccess(event: CommandEvent) { } // we don't always need to handle the success
}