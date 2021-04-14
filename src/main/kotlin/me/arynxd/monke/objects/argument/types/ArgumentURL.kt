package me.arynxd.monke.objects.argument.types

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.command.CommandEvent
import java.net.MalformedURLException
import java.net.URL

class ArgumentURL(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: ArgumentType,
    override val condition: (URL) -> Boolean = { true },

    ) : Argument<URL>() {

    override suspend fun convert(input: String, event: CommandEvent): URL? {
        return try {
            return withContext(Dispatchers.IO) { URL(input) }
        }
        catch (exception: MalformedURLException) {
            null
        }
    }
}