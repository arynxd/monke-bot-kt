package me.arynxd.monke.objects.argument.types

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentResult
import me.arynxd.monke.objects.argument.Type
import me.arynxd.monke.objects.command.CommandEvent
import java.net.MalformedURLException
import java.net.URL

class ArgumentURL(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (URL) -> ArgumentResult<URL> = { ArgumentResult(it, null) }

) : Argument<URL>() {

    override suspend fun convert(input: String, event: CommandEvent): ArgumentResult<URL> {
        return try {
            return ArgumentResult(withContext(Dispatchers.IO) { URL(input) }, null)
        }
        catch (exception: MalformedURLException) {
            ArgumentResult(null, "Invalid URL or URL not found")
        }
    }
}


