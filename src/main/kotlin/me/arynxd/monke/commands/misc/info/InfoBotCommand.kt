package me.arynxd.monke.commands.misc.info

import me.arynxd.monke.MONKE_VERSION
import me.arynxd.monke.handlers.translate
import me.arynxd.monke.handlers.translateAll
import me.arynxd.monke.handlers.translationStage
import me.arynxd.monke.objects.command.*
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.objects.command.threads.CommandReply
import me.arynxd.monke.util.classes.MonkeInfo

class InfoBotCommand(parent: Command) : SubCommand(
    parent,
    CommandMetaData(
        name = "bot",
        description = "Shows info about the bot.",
        category = CommandCategory.MISC
    )
) {
    override fun runSync(event: CommandEvent) {
        val jda = event.jda
        val language = event.language

        val translations = translateAll(language,
            translationStage { path = "command.info.child.bot.keyword.jvm_version" },

            translationStage { path = "command.info.child.bot.keyword.jda_version" },

            translationStage { path = "command.info.child.bot.keyword.monke_version" },

            translationStage { path = "command.info.child.bot.keyword.thread_count" },

            translationStage { path = "command.info.child.bot.keyword.memory_usage" },

            translationStage { path = "command.info.child.bot.keyword.cpu_usage" },

            translationStage { path = "command.info.child.bot.keyword.total_users" },

            translationStage { path = "command.info.child.bot.keyword.total_servers" },

            translationStage { path = "command.info.child.bot.keyword.uptime" }
        )

        val jvmVersion = translations[0]
        val jdaVersion = translations[1]
        val monkeVersion = translations[2]

        val threadCount = translations[3]
        val memoryUsage = translations[4]
        val cpuUsage = translations[5]

        val totalUsers = translations[6]
        val totalServers = translations[7]
        val uptime = translations[8]

        event.replyAsync {
            type(CommandReply.Type.INFORMATION)
            title("${event.jda.selfUser.name} information")

            field(jvmVersion, MonkeInfo.getJavaVersion(), true)
            field(jdaVersion, MonkeInfo.getJDAVersion(), true)
            field(monkeVersion, MONKE_VERSION, true)

            field(threadCount, MonkeInfo.getThreadCount(), true)
            field(memoryUsage, MonkeInfo.getMemoryFormatted(), true)
            field(cpuUsage, "${MonkeInfo.getCPUUsage()}%", true)

            field(totalUsers, MonkeInfo.getUserCount(jda), true)
            field(totalServers, MonkeInfo.getGuildCount(jda), true)
            field(uptime, MonkeInfo.getUptimeString(), true)

            footer()
            event.thread.post(this)
        }
    }
}