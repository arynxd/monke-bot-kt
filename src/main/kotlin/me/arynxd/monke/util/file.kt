package me.arynxd.monke.util

import me.arynxd.monke.Monke
import net.dv8tion.jda.api.entities.Icon
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL

fun loadResource(fileName: String): InputStream {
    return try {
        Monke::class.java.classLoader.getResourceAsStream(fileName) ?: InputStream.nullInputStream()
    }
    catch (exception: IOException) {
        InputStream.nullInputStream()
    }
}

fun convertToString(input: InputStream): String {
    val builder = StringBuilder()
    val reader = BufferedReader(InputStreamReader(input))
    var line: String?

    line = reader.readLine()

    while (line != null) {
        builder.append(line)
        line = reader.readLine()
    }

    reader.close()
    input.close()

    return builder.toString()
}

fun getIcon(url: URL): Icon? {
    return try {
        url.openStream().use {
            Icon.from(it)
        }
    }
    catch (exception: IOException) {
        return null
    }
}