package me.arynxd.monke.handlers.translation

import me.arynxd.monke.objects.translation.Language

fun translate(fn: TranslationBuilder.() -> Unit): String {
    val builder = TranslationBuilder(null, null, emptyArray(), false)
    fn(builder)
    return builder.build()
}

fun translate(lang: Language, fn: TranslationBuilder.() -> Unit): String {
    val builder = TranslationBuilder(lang, null, emptyArray(), false)
    fn(builder)
    return builder.build()
}

fun translateInternal(fn: TranslationBuilder.() -> Unit): String {
    val builder = TranslationBuilder(TranslationHandler.internalLang, null, emptyArray(), true)
    fn(builder)
    return builder.build()
}

fun translationStep(fn: TranslationBuilder.() -> Unit): TranslationBuilder {
    val builder = TranslationBuilder(null, null, emptyArray(), false)
    fn(builder)
    return builder
}

fun translationStepInternal(fn: TranslationBuilder.() -> Unit): TranslationBuilder {
    val builder = TranslationBuilder(TranslationHandler.internalLang, null, emptyArray(), true)
    fn(builder)
    return builder
}

fun translateAll(lang: Language, fn: MultiPartTranslationBuilder.() -> Unit): List<String> {
    val builder = MultiPartTranslationBuilder(lang)
    fn(builder)
    return builder.buildAll()
}

fun translateAllInternal(fn: MultiPartTranslationBuilder.() -> Unit): List<String> {
    val builder = MultiPartTranslationBuilder(TranslationHandler.internalLang)
    fn(builder)
    return builder.buildAll()
}