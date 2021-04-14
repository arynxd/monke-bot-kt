package me.arynxd.monke.commands.misc.info

import me.arynxd.monke.MONKE_VERSION
import me.arynxd.monke.handlers.TranslationHandler
import me.arynxd.monke.objects.command.*

class InfoBotCommand(parent: Command) : SubCommand(
    name = "bot",
    description = "Shows info about the bot.",
    category = CommandCategory.MISC,
    parent = parent,
) {
    override fun runSync(event: CommandEvent) {
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

        event.replyAsync {
            type(CommandReply.Type.INFORMATION)
            title("${event.jda.selfUser.name} information")

            field(jvmVersion, monke.getJavaVersion(), true)
            field(jdaVersion, monke.getJDAVersion(), true)
            field(monkeVersion, MONKE_VERSION, true)

            field(threadCount, monke.getThreadCount().toString(), true)
            field(memoryUsage, monke.getMemoryFormatted(), true)
            field(cpuUsage, "${monke.getCPUUsage()}%", true)

            field(totalUsers, monke.getUserCount().toString(), true)
            field(totalServers, monke.getGuildCount().toString(), true)
            field(uptime, monke.getUptimeString(), true)

            footer()
            send()
        }
    }
}