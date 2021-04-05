package me.arynxd.monke.commands.developer

import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.argument.types.ArgumentLong
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.util.prettyPrintJson
import me.arynxd.monke.util.splitStringCodeblock
import net.dv8tion.jda.api.requests.Request
import net.dv8tion.jda.api.requests.Response
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.dv8tion.jda.internal.requests.Route

@Suppress("UNUSED")
class JsonCommand : Command(
    name = "json",
    description = "Gets the JSON representation of a message.",
    category = CommandCategory.DEVELOPER,
    aliases = listOf("getjson"),
    flags = listOf(CommandFlag.DEVELOPER_ONLY),

    arguments = ArgumentConfiguration(
        listOf(
            ArgumentLong(
                name = "message-id",
                description = "The ID to get from. Must be a message from the current channel.",
                required = true,
                type = ArgumentType.REGULAR
            ),
        )
    ),

    ) {
    override suspend fun run(event: CommandEvent) {
        val channel = event.channel
        val jda = event.jda
        val id = event.getArgument<Long>(0).toString()

        RestActionImpl<Any>(
            jda,
            Route.Messages.GET_MESSAGE.compile(channel.id, id)
        ) { response: Response, _: Request<Any?>? ->
            val json = splitStringCodeblock(prettyPrintJson(response.getObject().toString())).map {
                "```json\n${
                    it.replace("`", "")
                        .replace("\\\"", "\"")
                }```"
            }

            event.replyAsync {
                type(CommandReply.Type.SUCCESS)
                footer()
                chunks(json)
            }
        }.queue(null) {
            event.replyAsync {
                type(CommandReply.Type.EXCEPTION)
                title(
                    TranslationHandler.getString(
                        language = event.getLanguage(),
                        key = "command.json.message_not_found"
                    )
                )
                footer()
                send()
            }
        }
    }
}