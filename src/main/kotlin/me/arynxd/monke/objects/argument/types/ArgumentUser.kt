package me.arynxd.monke.objects.argument.types

import dev.minn.jda.ktx.await
import me.arynxd.monke.objects.argument.Argument
import me.arynxd.monke.objects.argument.ArgumentType
import me.arynxd.monke.objects.command.CommandEvent
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.ErrorResponseException

class ArgumentUser(
    override val name: String,
    override val description: String,
    override val required: Boolean,
    override val type: ArgumentType,
    override val condition: (User) -> Boolean = { true },
) : Argument<User>() {

    override suspend fun convert(input: String, event: CommandEvent): User? {
        val memberMentions = event.message.mentionedUsers.filter { !it.equals(event.jda.selfUser) }

        if (memberMentions.isNotEmpty()) { //Direct mention
            return memberMentions[0]
        }

        val memberId = input.toLongOrNull()

        if (memberId != null) { //ID
            return try {
                event.jda.retrieveUserById(memberId).await()
            } catch (exception: ErrorResponseException) {
                return null
            }
        }

        val memberNames = event.guild.retrieveMembersByPrefix(input, 1).await()

        if (memberNames.isNotEmpty()) { //Name
            return memberNames[0].user
        }

        return null
    }
}