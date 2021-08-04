package me.arynxd.monke.objects.argument.impl

import dev.minn.jda.ktx.await
import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentResult
import me.arynxd.monke.objects.command.CommandEvent
import me.arynxd.monke.util.equalsIgnoreCase
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.ErrorResponseException

class ArgumentGuild(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (Guild) -> ArgumentResult<Guild> = { ArgumentResult(it, null) }

) : Argument<Guild>() {
    override suspend fun convert(input: String, event: CommandEvent): ArgumentResult<Guild> {
        if (input.equalsIgnoreCase("this")) {
            return ArgumentResult(event.guild, null)
        }

        val byName = event.jda.guildCache.find { it.name.equals(input, true) }

        val toLong = input.toLongOrNull()
        val byId = event.jda.guildCache.find { it.idLong == toLong }

        if (byId != null) {
            return ArgumentResult.ofSuccess(byId)
        }

        if (byName != null) {
            return ArgumentResult.ofSuccess(byName)
        }

        return ArgumentResult.ofFailure("command.argument.guild.error.not_found", input)
    }
}

class ArgumentMember(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (Member) -> ArgumentResult<Member> = { ArgumentResult(it, null) }

) : Argument<Member>() {

    override suspend fun convert(input: String, event: CommandEvent): ArgumentResult<Member> {
        val memberMentions = event.message.mentionedMembers.toMutableList()

        if (isBotMention(event)) {
            memberMentions.removeAt(0)
        }

        if (memberMentions.isNotEmpty()) { //Direct mention
            return ArgumentResult(memberMentions[0], null)
        }

        val memberId = input.toLongOrNull()

        if (memberId != null) { //ID
            return try {
                ArgumentResult.ofSuccess(event.guild.retrieveMemberById(memberId).await())
            }
            catch (exception: ErrorResponseException) {
                return ArgumentResult.ofFailure("command.argument.member.error.id_not_found", memberId)
            }
        }

        val memberNames = event.guild.retrieveMembersByPrefix(input, 10).await()

        if (memberNames.isNotEmpty()) { //Name
            return ArgumentResult.ofSuccess(memberNames[0])
        }

        return ArgumentResult.ofFailure("command.argument.member.error.name_not_found", input)
    }
}

class ArgumentUser(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: Type,
    override val condition: (User) -> ArgumentResult<User> = { ArgumentResult(it, null) }
) : Argument<User>() {

    override suspend fun convert(input: String, event: CommandEvent): ArgumentResult<User> {
        val memberMentions = event.message.mentionedMembers.toMutableList()

        if (isBotMention(event)) {
            memberMentions.removeAt(0)
        }

        if (memberMentions.isNotEmpty()) { //Direct mention
            return ArgumentResult.ofSuccess(memberMentions[0].user)
        }

        val memberId = input.toLongOrNull()

        if (memberId != null) { //ID
            return try {
                ArgumentResult.ofSuccess(event.jda.retrieveUserById(memberId).await())
            }
            catch (exception: ErrorResponseException) {
                return ArgumentResult.ofFailure("command.argument.user.error.id_not_found", memberId)
            }
        }

        val memberNames = event.guild.retrieveMembersByPrefix(input, 10).await()

        if (memberNames.isNotEmpty()) { //Name
            return ArgumentResult(memberNames[0].user, null)
        }

        return ArgumentResult.ofFailure("command.argument.user.error.name_not_found", input)
    }
}

private fun isBotMention(event: CommandEvent): Boolean {
    val content = event.message.contentRaw
    val id = event.jda.selfUser.idLong
    return content.startsWith("<@$id>") || content.startsWith("<@!$id>")
}