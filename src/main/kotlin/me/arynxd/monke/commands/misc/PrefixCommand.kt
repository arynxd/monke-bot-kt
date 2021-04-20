package me.arynxd.monke.commands.misc

import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.argument.types.ArgumentString
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandMetaData
import me.arynxd.monke.objects.command.CommandReply
import me.arynxd.monke.objects.events.types.command.CommandEvent

@Suppress("UNUSED")
class PrefixCommand : Command(
    CommandMetaData(
        name = "prefix",
        description = "Gets and sets the prefix for this server.",
        category = CommandCategory.MISC,

        arguments = ArgumentConfiguration(
            listOf(
                ArgumentString(
                    name = "prefix",
                    description = "The new prefix. 5 characters or less.",
                    required = false,
                    type = Type.REGULAR,
                    condition = { it.length <= 5 }
                )
            )
        )
    )
) {
    override fun runSync(event: CommandEvent) {
        val cache = event.getDataCache()
        val language = event.getLanguage()

        if (!event.isArgumentPresent(0)) {
            event.replyAsync {
                type(CommandReply.Type.INFORMATION)
                title(
                    translate(
                        language = language,
                        key = "command.prefix.response.prefix_here",
                        values = arrayOf(cache.prefix)
                    )
                )
                send()
            }
            return
        }

        val prefix = event.getArgument<String>(0)

        if (prefix == cache.prefix) {
            event.replyAsync {
                type(CommandReply.Type.EXCEPTION)
                title(
                    translate(
                        language = language,
                        key = "command.prefix.response.prefix_already",
                        values = arrayOf(prefix)
                    )
                )
                send()
            }
            return
        }

        event.replyAsync {
            type(CommandReply.Type.SUCCESS)
            title(
                translate(
                    language = language,
                    key = "command.prefix.response.prefix_new",
                    values = arrayOf(prefix)
                )
            )
            send()
            cache.prefix = prefix
        }
    }
}