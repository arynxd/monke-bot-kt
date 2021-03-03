package me.arynxd.monke.objects.exception

import java.lang.RuntimeException

class HandlerNotFoundException(message: String): RuntimeException(message)

class TranslationException(message: String): RuntimeException(message)