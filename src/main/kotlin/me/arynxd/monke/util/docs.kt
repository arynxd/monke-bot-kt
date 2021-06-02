package me.arynxd.monke.util

import com.overzealous.remark.Options
import com.overzealous.remark.Remark
import me.arynxd.monke.objects.docs.JDocParser

const val JAVA_DOCS_URL = "https://docs.oracle.com/javase/8/docs/api/"
private val codeblockPattern = Regex("(```java\n)(.*?)(```)")
private val remark = makeRemark()

private fun makeRemark(): Remark {
    val remarkOptions = Options.github().apply {
        this.inlineLinks = true
        this.fencedCodeBlocksWidth = 3
    }
    return Remark(remarkOptions)
}

fun formatText(docs: String?, currentUrl: String?): String {
    var markdown: String = remark.convertFragment(fixSpaces(docs), currentUrl)

    //remove unnecessary carriage return chars
    markdown = markdown.replace("\r", "")

    //fix codeblocks
    markdown = markdown.replace("\n\n```", "\n\n```java")
    val matcher = codeblockPattern.toPattern().matcher(markdown)
    if (matcher.find()) {
        val buffer = StringBuffer()
        do {
            matcher.appendReplacement(
                buffer,
                (matcher.group(1) + matcher.group(2))
                    .replace("\n\\s", "\n") + matcher.group(3)
            )
        } while (matcher.find())
        matcher.appendTail(buffer)
        markdown = buffer.toString()
    }

    //remove too many newlines (max 2)
    markdown = markdown.replace("\n{3,}".toRegex(), "\n\n")
    return markdown
}

fun fixSpaces(input: String?): String {
    return input?.replace("\\h".toRegex(), " ")?: "null"
}

fun getLink(jdocBase: String, doc: JDocParser.ClassDocumentation) =
    getLink(jdocBase, doc.pack.toString(), doc.className.toString())

fun getLink(jdocBase: String, classPackage: String, className: String) =
    jdocBase + classPackage.replace(".", "/") + '/' + className + ".html"

fun fixUrl(url: String) = url.replace(")", "%29") //as markdown doesn't allow ')' in urls

fun fixSignature(sig: String): String {
    return sig.replace("\u200B", "")
        .replace(Regex("\\b(?:[a-z]+\\.)+([A-Z])"), "$1")
        .replace(Regex("\\s{2,}"), " ")
}