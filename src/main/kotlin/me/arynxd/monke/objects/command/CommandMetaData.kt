package me.arynxd.monke.objects.command

import me.arynxd.monke.objects.argument.ArgumentConfiguration
import me.arynxd.monke.objects.command.precondition.Precondition
import net.dv8tion.jda.api.Permission

data class CommandMetaData @JvmOverloads constructor(
    val name: String,
    val description: String,
    val category: CommandCategory,

    val aliases: List<String> = emptyList(),
    val flags: List<CommandFlag> = emptyList(),
    val arguments: ArgumentConfiguration = ArgumentConfiguration(),
    var isDisabled: Boolean = false,
    val cooldown: Long = 1000L,

    val preconditions: List<Precondition> = emptyList(),

    val memberPermissions: List<Permission> = emptyList(),
    val botPermissions: List<Permission> = emptyList(),
)
