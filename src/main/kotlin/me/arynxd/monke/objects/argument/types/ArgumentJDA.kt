package me.arynxd.monke.objects.argument.types

import dev.minn.jda.ktx.await
import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.events.types.command.CommandEvent
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.exceptions.ErrorResponseException

class ArgumentServer(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: ArgumentType,
    override val condition: (Guild) -> Boolean = { true },

    ) : Argument<Guild>() {

    override suspend fun convert(input: String, event: CommandEvent): Guild? {
        if (input.equals("this", true)) {
            return event.guild
        }

        val byName = event.monke.jda.guildCache.find { it.name.equals(input, true) }

        val toLong = input.toLongOrNull() ?: return byName
        val byId = event.monke.jda.guildCache.find { it.idLong == toLong }

        if (byId != null) {
            return byId
        }

        if (byName != null) {
            return byName
        }
        return null
    }
}

class ArgumentMember(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: ArgumentType,
    override val condition: (Member) -> Boolean = { true },
) : Argument<Member>() {

    override suspend fun convert(input: String, event: CommandEvent): Member? {
        val memberMentions = event.message.mentionedMembers.toMutableList()

        if (isBotMention(event)) {
            memberMentions.removeAt(0)
        }

        if (memberMentions.isNotEmpty()) { //Direct mention
            return memberMentions[0]
        }

        val memberId = input.toLongOrNull()

        if (memberId != null) { //ID
            return try {
                event.guild.retrieveMemberById(memberId).await()
            }
            catch (exception: ErrorResponseException) {
                return null
            }
        }

        val memberNames = event.guild.retrieveMembersByPrefix(input, 1).await()

        if (memberNames.isNotEmpty()) { //Name
            return memberNames[0]
        }

        return null
    }

    private fun isBotMention(event: CommandEvent): Boolean {
        val content = event.message.contentRaw
        val id = event.jda.selfUser.idLong
        return content.startsWith("<@$id>") || content.startsWith("<@!$id>")
    }
}