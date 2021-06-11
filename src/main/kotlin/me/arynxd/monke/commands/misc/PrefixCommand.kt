package me.arynxd.monke.commands.misc

import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentResult
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.CommandMetaData
import me.arynxd.monke.objects.command.threads.CommandReply

@Suppress("UNUSED")
class PrefixCommand : Command(
    CommandMetaData(
        name = "prefix",
        description = "Gets and sets the prefix for this server.",
        category = CommandCategory.MISC,

        arguments = ArgumentConfiguration(
            ArgumentString(
                name = "prefix",
                description = "The new prefix. 5 characters or less.",
                required = false,
                type = Type.REGULAR,
                condition = {
                    if (it.length >= 5)
                        ArgumentResult(null, "Prefix must be less than 6 characters")
                    else
                        ArgumentResult(it, null)
                }
            )
        )
    )
) {
    override fun runSync(event: CommandEvent) {
        val cache = event.dataCache
        val language = event.language

        if (!event.isArgumentPresent(0)) {
            event.replyAsync {
                type(CommandReply.Type.INFORMATION)
                title(
                    translate {
                        lang = language
                        path = "command.prefix.response.prefix_here"
                        values = arrayOf(cache.prefix)
                    }
                )
                event.thread.post(this)
            }
            return
        }

        val prefix = event.argument<String>(0)

        if (prefix == cache.prefix) {
            event.replyAsync {
                type(CommandReply.Type.EXCEPTION)
                title(
                    translate {
                        lang = language
                        path = "command.prefix.response.prefix_already"
                        values = arrayOf(prefix)
                    }
                )
                event.thread.post(this)
            }
            return
        }

        event.replyAsync {
            type(CommandReply.Type.SUCCESS)
            title(
                translate {
                    lang = language
                    path = "command.prefix.response.prefix_new"
                    values = arrayOf(prefix)
                }
            )
            event.thread.post(this)
            cache.prefix = prefix
        }
    }
}