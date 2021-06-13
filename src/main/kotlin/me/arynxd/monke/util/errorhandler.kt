package me.arynxd.monke.util

import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse

val IGNORE_UNKNOWN = ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE)