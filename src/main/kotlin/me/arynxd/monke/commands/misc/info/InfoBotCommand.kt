package me.arynxd.monke.commands.misc.info

import me.arynxd.monke.MONKE_VERSION
import me.arynxd.monke.handlers.translate
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.threads.CommandReply

class InfoBotCommand(parent: Command) : SubCommand(
    parent,
    CommandMetaData(
        name = "bot",
        description = "Shows info about the bot.",
        category = CommandCategory.MISC
    )
) {
    override fun runSync(event: CommandEvent) {
        val monke = event.monke
        val language = event.language()

        val jvmVersion = translate(language, "command.info.child.bot.keyword.jvm_version")
        val jdaVersion = translate(language, "command.info.child.bot.keyword.jda_version")
        val monkeVersion = translate(language, "command.info.child.bot.keyword.monke_version")

        val threadCount = translate(language, "command.info.child.bot.keyword.thread_count")
        val memoryUsage = translate(language, "command.info.child.bot.keyword.memory_usage")
        val cpuUsage = translate(language, "command.info.child.bot.keyword.cpu_usage")

        val totalUsers = translate(language, "command.info.child.bot.keyword.total_users")
        val totalServers = translate(language, "command.info.child.bot.keyword.total_servers")
        val uptime = translate(language, "command.info.child.bot.keyword.uptime")

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