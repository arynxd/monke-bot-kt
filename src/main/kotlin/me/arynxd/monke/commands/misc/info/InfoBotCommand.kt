package me.arynxd.monke.commands.misc.info

import dev.minn.jda.ktx.Embed
import me.arynxd.monke.MONKE_VERSION
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.command.Command
import me.arynxd.monke.objects.command.CommandCategory
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.SubCommand
import net.dv8tion.jda.api.entities.MessageEmbed

class InfoBotCommand(parent: Command) : SubCommand(
    name = "bot",
    description = "Shows info about the bot.",
    category = CommandCategory.MISC,
    parent = parent,
) {
    override suspend fun run(event: CommandEvent) {
        val monke = event.monke
        val language = event.getLanguage()

        val jvmVersion = TranslationHandler.getString(language, "command.info.child.bot.keyword.jvm_version")
        val jdaVersion = TranslationHandler.getString(language, "command.info.child.bot.keyword.jda_version")
        val monkeVersion = TranslationHandler.getString(language, "command.info.child.bot.keyword.monke_version")

        val threadCount = TranslationHandler.getString(language, "command.info.child.bot.keyword.thread_count")
        val memoryUsage = TranslationHandler.getString(language, "command.info.child.bot.keyword.memory_usage")
        val cpuUsage = TranslationHandler.getString(language, "command.info.child.bot.keyword.cpu_usage")

        val totalUsers = TranslationHandler.getString(language, "command.info.child.bot.keyword.total_users")
        val totalServers = TranslationHandler.getString(language, "command.info.child.bot.keyword.total_servers")
        val uptime = TranslationHandler.getString(language, "command.info.child.bot.keyword.uptime")

        event.sendEmbed(Embed(
            title = "${event.jda.selfUser.name} information",
            fields = listOf(
                MessageEmbed.Field(jvmVersion, monke.getJavaVersion(), true),
                MessageEmbed.Field(jdaVersion, monke.getJDAVersion(), true),
                MessageEmbed.Field(monkeVersion, MONKE_VERSION, true),

                MessageEmbed.Field(threadCount, monke.getThreadCount().toString(), true),
                MessageEmbed.Field(memoryUsage, "${monke.getMemoryFormatted()} [ ${monke.getMemoryPercent()}% ]", true),
                MessageEmbed.Field(cpuUsage, "${monke.getCPUUsage()}%", true),

                MessageEmbed.Field(totalUsers, monke.getUserCount().toString(), true),
                MessageEmbed.Field(totalServers, monke.getGuildCount().toString(), true),
                MessageEmbed.Field(uptime, monke.getUptimeString(), true)
            )
        ))
    }
}