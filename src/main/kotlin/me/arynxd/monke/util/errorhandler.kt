package me.arynxd.monke.util

import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse

fun ignoreUnknown() =
    ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE)