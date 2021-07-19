package me.arynxd.monke.util.classes

import me.arynxd.monke.util.parseUptime
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDAInfo
import java.lang.management.ManagementFactory
import java.text.DecimalFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object MonkeInfo {
    private val cpuFormatter = DecimalFormat("#.##")

    val jdaVersion: String = JDAInfo.VERSION
    val javaVersion: String = System.getProperty("java.version")

    val maxMemory: Long
        get() = Runtime.getRuntime().maxMemory()

    val freeMemory: Long
        get() = Runtime.getRuntime().freeMemory()

    val totalMemory: Long
        get() = Runtime.getRuntime().totalMemory()

    val threadCount: Int
        get() = ManagementFactory.getThreadMXBean().threadCount

    val memoryFormatted: String
        get() = (totalMemory - freeMemory shr 20).toString() + "MB / " + (maxMemory shr 20) + "MB"

    val cpuUsageFormatted: String
        get() = cpuFormatter.format(ManagementFactory.getOperatingSystemMXBean().systemLoadAverage)

    val uptimeString: String
        get() {
            val millis = ManagementFactory.getRuntimeMXBean().uptime
            return parseUptime(
                Duration.between(
                    LocalDateTime.now().minus(millis, ChronoUnit.MILLIS),
                    LocalDateTime.now()
                )
            )
        }

    fun getGuildCount(jda: JDA): Long {
        return jda.guildCache.size()
    }

    fun getUserCount(jda: JDA): Long {
        return jda.guildCache.sumOf { it.memberCount }.toLong()
    }
}