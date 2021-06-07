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
    fun getUserCount(jda: JDA): Long {
        return jda.guildCache.sumBy { it.memberCount }.toLong()
    }

    fun getJDAVersion(): String {
        return JDAInfo.VERSION
    }

    fun getJavaVersion(): String {
        return System.getProperty("java.version")
    }

    fun getMaxMemory(): Long {
        return Runtime.getRuntime().maxMemory()
    }

    fun getFreeMemory(): Long {
        return Runtime.getRuntime().freeMemory()
    }

    fun getTotalMemory(): Long {
        return Runtime.getRuntime().totalMemory()
    }

    fun getThreadCount(): Int {
        return ManagementFactory.getThreadMXBean().threadCount
    }

    fun getGuildCount(jda: JDA): Long {
        return jda.guildCache.size()
    }

    fun getMemoryFormatted(): String {
        return (getTotalMemory() - getFreeMemory() shr 20).toString() + "MB / " + (getMaxMemory() shr 20) + "MB"
    }

    fun getCPUUsage(): String {
        return DecimalFormat("#.##").format(ManagementFactory.getOperatingSystemMXBean().systemLoadAverage)
    }

    fun getUptimeString(): String {
        val millis = ManagementFactory.getRuntimeMXBean().uptime
        return parseUptime(
            Duration.between(
                LocalDateTime.now().minus(millis, ChronoUnit.MILLIS),
                LocalDateTime.now()
            )
        )
    }
}