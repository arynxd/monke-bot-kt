package me.arynxd.monke.objects.docs

import me.arynxd.monke.util.*
import org.apache.commons.collections4.OrderedMap
import org.apache.commons.collections4.map.ListOrderedMap
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.stream.Collectors


object JDocParser {

    //return, funcName, parameters
    val METHOD_PATTERN = Regex("([a-zA-Z.<>?\\[\\]]+)\\s+([a-zA-Z][a-zA-Z0-9]+)\\(([@a-zA-Z0-9\\s.,<>?\\[\\]]*)\\)")

    //annotations in front of method
    val ANNOTATION_PATTERN = Regex("^((?:@[^\n]+\n)+)")

    //annotation splitter
    val ANNOTATION_PARTS = Regex("@([a-zA-Z]+)(\\(\\S*\\))?\n")

    //type, name
    val METHOD_ARG_PATTERN = Regex("(?:[a-z]+\\.)*([a-zA-Z][a-zA-Z0-9.<,?>\\[\\]]*)\\s+([a-zA-Z][a-zA-Z0-9]*)(?:\\s*,|$)")

    //used for inner classes being made available as top-level search if applicable
    const val SUBCLASSES_MAP_KEY = "#JDOC_SUBCLASSES_KEY#"

    private fun getSingleElementByClass(root: Element, className: String): Element {
        val elementsByClass = root.getElementsByClass(className)
        if (elementsByClass.size != 1) {
            val error = "Found " + elementsByClass.size
                .toString() + " elements with class " + className + " inside of " + root.tagName()
                .toString() + "-" + root.className()
            throw RuntimeException(error + root.html())
        }
        return elementsByClass.first()
    }

    private fun getSingleElementByQuery(root: Element, query: String): Element {
        val elementsByQuery = root.select(query)
        if (elementsByQuery.size > 1) {
            val error = "Found " + elementsByQuery.size
                .toString() + " elements matching query \"" + query + "\" inside of " + root.tagName()
                .toString() + "-" + root.className()
            throw RuntimeException(error + root.html())
        }
        return elementsByQuery.first()
    }

    fun parse(jdocBase: String, name: String, inputStream: InputStream, docs: MutableMap<String?, ClassDocumentation>) {
        val pathSplits = name.split("/".toRegex()).toTypedArray()
        val fileName = pathSplits[pathSplits.size - 1]
        if (!Character.isUpperCase(fileName[0])) {
            //ignore jdoc structure html
            return
        }
        val nameSplits = fileName.split("\\.".toRegex()).toTypedArray()
        val className = nameSplits[nameSplits.size - 2]
        val fullName = fileName.substring(0, fileName.length - nameSplits[nameSplits.size - 1].length - 1)
        BufferedReader(InputStreamReader(inputStream)).use { buffer ->
            //create dom Document
            val content = buffer.lines().collect(Collectors.joining("\n"))
            val document = Jsoup.parse(content)

            //classDocument (classname, package, description)
            val titleElem: Element = getSingleElementByClass(document, "title")
            val classSig = fixSpaces(titleElem.text())
            var packageElem: Element = titleElem.previousElementSibling()
            if (packageElem.children().size > 1) {
                packageElem = packageElem.children().last()
            }
            val pack= fixSpaces(packageElem.text())
            val link = getLink(jdocBase, pack, fullName)
            var descriptionElement: Element? = null
            val descriptionCandidates = document.select(".description .block")
            if (descriptionCandidates.size > 1) {
                val removed = descriptionCandidates
                    .map { it.child(0) }
                    .filter { it != null && !it.className().startsWith("deprecat") }
                    .map { it.parent() }

                if (removed.size != 1) throw RuntimeException("Found too many description candidates")
                descriptionElement = removed[0]
            } else if (descriptionCandidates.size == 1) {
                descriptionElement = descriptionCandidates[0]
            }
            val description = if (descriptionElement == null) "" else formatText(descriptionElement.html(), link)
            val classDoc = ClassDocumentation(pack, fullName, classSig, description, classSig.startsWith("Enum"))

            //methods, fields
            val details = document.getElementsByClass("details").first()
            if (details != null) {
                //methods
                var tmp: Element = getSingleElementByQuery(details, "a[name=\"method.detail\"], a[id=\"method.detail\"]")
                var docBlock = getDocBlock(jdocBase, tmp, classDoc)
                if (docBlock != null) {
                    for (block in docBlock) {
                        val mdocs = classDoc.methodDocs.computeIfAbsent(block.title.toLowerCase()) { HashSet() }
                        mdocs.add(
                            MethodDocumentation(
                                classDoc,
                                block.signature,
                                block.hashLink,
                                block.description,
                                block.fields
                            )
                        )
                    }
                }
                //vars
                tmp = getSingleElementByQuery(details, "a[name=\"field.detail\"], a[id=\"field.detail\"]")
                docBlock = getDocBlock(jdocBase, tmp, classDoc)
                if (docBlock != null) {
                    for (block in docBlock) {
                        classDoc.classValues[block.title.toLowerCase()] = ValueDocumentation(
                            classDoc,
                            block.title,
                            block.hashLink,
                            block.signature,
                            block.description
                        )
                    }
                }
                //enum-values
                tmp =
                    getSingleElementByQuery(details, "a[name=\"enum.constant.detail\"], a[id=\"enum.constant.detail\"]")
                docBlock = getDocBlock(jdocBase, tmp, classDoc)
                if (docBlock != null) {
                    for (block in docBlock) {
                        classDoc.classValues[block.title.toLowerCase()] = ValueDocumentation(
                            classDoc,
                            block.title,
                            block.hashLink,
                            block.signature,
                            block.description
                        )
                    }
                }
            }
            val methodSummary = getSingleElementByQuery(document, "a[name=\"method.summary\"], a[id=\"method.summary\"]")
            classDoc.inheritedMethods.putAll(getInheritedMethods(methodSummary)!!)

            //storing
            if (nameSplits.size > 2) {
                var parent = docs.computeIfAbsent(
                    nameSplits[0].toLowerCase()
                ) {
                    ClassDocumentation(
                        null,
                        null,
                        null,
                        null,
                        false
                    )
                }
                for (i in 1 until nameSplits.size - 2) {
                    parent = parent.subClasses.computeIfAbsent(
                        nameSplits[i].toLowerCase()
                    ) {
                        ClassDocumentation(
                            null,
                            null,
                            null,
                            null,
                            false
                        )
                    }
                }
                if (parent.subClasses.containsKey(className.toLowerCase())) classDoc.subClasses.putAll(
                    parent.subClasses[className.toLowerCase()]!!.subClasses
                )
                parent.subClasses[className.toLowerCase()] = classDoc

                //store for later subclass indexing
                val actualClassName = nameSplits[nameSplits.size - 2].toLowerCase()
                val subClassesNode = docs.computeIfAbsent(SUBCLASSES_MAP_KEY) {
                    ClassDocumentation(
                        null,
                        null,
                        null,
                        null,
                        false
                    )
                }
                val subClassElem = subClassesNode.subClasses[actualClassName]

                when {
                    subClassElem == null -> {
                        subClassesNode.subClasses[actualClassName] = classDoc
                    }
                    subClassElem.shortTitle != null -> {
                        subClassesNode.subClasses[actualClassName] = ClassDocumentation(null, null, null, null, false)
                    }

                    else -> {}
                }
            }
            else {
                //top-level class, store in map's root
                val current = docs[className.toLowerCase()]
                if (current?.shortTitle != null) throw RuntimeException(
                    String.format(
                        "Got a class-name conflict with classes %s.%s AND %s.%s",
                        classDoc.pack, classDoc.className, current.pack, current.className
                    )
                )
                if (current != null) classDoc.subClasses.putAll(current.subClasses)
                docs.put(className.toLowerCase(), classDoc)
            }
        }
        inputStream.close()
    }

    private fun getInheritedMethods(summaryAnchor: Element?): Map<String, String>? {
        var anchor = summaryAnchor
        val inherited = mutableMapOf<String, String>()
        if (anchor == null) return inherited
        anchor = anchor.parent()
        val inheritAnchors =
            anchor.select("a[name^=\"methods.inherited.from.class\"], a[id^=\"methods.inherited.from.class\"]")
        for (inheritAnchor in inheritAnchors) {
            if (inheritAnchor.siblingElements().size != 2) continue  //no methods shown as inherited cuz everything overridden
            var next = inheritAnchor.nextElementSibling()
            if (!next.tagName().equals("h3")) throw RuntimeException(
                "Got unexpected html while parsing inherited methods from class " + inheritAnchor.attr(
                    "name"
                )
            )
            var sub = next.children().last()
            if (sub == null || !sub.tagName().equals("a")) continue
            val parent: String = sub.text().toLowerCase()
            next = next.nextElementSibling()
            if (!next.tagName().equals("code")) throw RuntimeException(
                "Got unexpected html while parsing inherited methods from class " + inheritAnchor.attr(
                    "name"
                )
            )
            sub = next.children().first()
            while (sub != null) {
                if (sub.tagName().equals("a")) {
                    inherited.putIfAbsent(sub.text().toLowerCase(), parent)
                }
                sub = sub.nextElementSibling()
            }
        }
        return inherited
    }

    private fun getDocBlock(jdocBase: String, elem: Element?, reference: ClassDocumentation): List<DocBlock>? {
        var el: Element? = elem?.nextElementSibling() ?: return null
        val baseLink: String = getLink(jdocBase, reference)
        val blocks = mutableListOf<DocBlock>()
        var hashLink: String? = null
        while (el != null) {
            if (el.tagName().equals("a")) {
                hashLink = '#' + el.id().ifEmpty { el!!.attr("name") }
            }
            else if (el.tagName().equals("ul")) {
                var tmp: Element = el.getElementsByTag("h4").first()
                val title: String = fixSpaces(tmp.text().trim())
                var description = ""
                var signature = ""
                val fields = ListOrderedMap<String, List<String>>()
                tmp = tmp.nextElementSibling()
                while (tmp != null) {
                    if (tmp.tagName().equals("pre")) {
                        //contains full signature
                        signature = fixSpaces(tmp.text().trim())
                    }
                    else if (tmp.tagName().equals("div") && tmp.className().equals("deprecationBlock")) {
                        //deprecation block(jdk11)
                        val deprecationElem = tmp.getElementsByClass("deprecationComment").first()
                        if (deprecationElem != null) {
                            fields["Deprecated:"] = listOf(formatText(deprecationElem.html(), baseLink))
                        }
                    }
                    else if (tmp.tagName().equals("div") && tmp.className().equals("block")) {
                        //main block of content (description or deprecation (prej11))
                        val deprecationElem = tmp.getElementsByClass("deprecationComment").first()
                        if (deprecationElem != null) {
                            //deprecation block
                            fields["Deprecated:"] = listOf(formatText(deprecationElem.html(), baseLink))
                        }
                        else {
                            //description block
                            description = formatText(tmp.html(), baseLink)
                        }
                    }
                    else if (tmp.tagName().equals("dl")) {
                        //a field
                        var fieldName: String? = null
                        var fieldValues: MutableList<String> = ArrayList()
                        for (element in tmp.children()) {
                            if (element.tagName().equals("dt")) {
                                if (fieldName != null) {
                                    fields[fieldName] = fieldValues
                                    fieldValues = ArrayList()
                                }
                                fieldName = fixSpaces(element.text().trim())
                            } else if (element.tagName().equals("dd")) {
                                fieldValues.add(formatText(element.html(), baseLink))
                            }
                        }
                        if (fieldName != null) {
                            fields[fieldName] = fieldValues
                        }
                    }
                    tmp = tmp.nextElementSibling()
                }
                blocks.add(DocBlock(title, hashLink, signature, description, fields))
            }
            el = el.nextElementSibling()
        }
        return blocks
    }
    interface Documentation {
        val title: String?
        val shortTitle: String?

        fun getUrl(jdocBase: String): String
        val content: String?
        val fields: Map<String, List<String>>?
            get() = null
    }

    private class DocBlock(
        val title: String,
        val hashLink: String?,
        val signature: String,
        val description: String,
        val fields: OrderedMap<String, List<String>>
    )

    class ClassDocumentation(
        val pack: String?,
        val className: String?,
        override val shortTitle: String?,
        override val content: String?,
        val isEnum: Boolean
    ) :
        Documentation {
        val methodDocs = mutableMapOf<String, MutableSet<MethodDocumentation>>()
        val subClasses = mutableMapOf<String, ClassDocumentation>()
        val classValues = mutableMapOf<String, ValueDocumentation>()
        val inheritedMethods: MutableMap<String, String> = HashMap()
        override val title: String?
            get() = shortTitle

        override fun getUrl(jdocBase: String): String {
            return getLink(jdocBase, this)
        }

        override val fields: Map<String, List<String>>?
            get() {
                if (!isEnum) return null
                val fields = mutableMapOf<String, List<String>>()
                fields["Values:"] = classValues.values.map { it.name }
                return fields
            }

    }

    class MethodDocumentation(
        val parent: ClassDocumentation,
        functionSig: String,
        private val hashLink: String?,
        override val content: String,
        override val fields: OrderedMap<String, List<String>>
    ) :
        Documentation {
        private val methodAnnos = mutableListOf<MethodAnnotation>()
        private val returnType: String
        private val functionName: String
        private val parameters: String
        private val functionSig: String
        private val argTypes = mutableListOf<String>()

        init {
            val methodMatcher = METHOD_PATTERN.toPattern().matcher(functionSig)
            if (!methodMatcher.find()) {
                throw RuntimeException("Got method with no proper method signature: $functionSig")
            }
            //check for documented annotations of method
            val annoGroupMatcher = ANNOTATION_PATTERN.toPattern().matcher(functionSig)
            if (annoGroupMatcher.find()) {
                val annoMatcher = ANNOTATION_PARTS.toPattern().matcher(annoGroupMatcher.group(1))
                while (annoMatcher.find()) {
                    methodAnnos.add(MethodAnnotation(annoMatcher.group(1), annoMatcher.group(2)))
                }
            }
            returnType = methodMatcher.group(1)
            functionName = methodMatcher.group(2)
            parameters = methodMatcher.group(3)
            this.functionSig = methodMatcher.group()
            val args = methodMatcher.group(3)
            val argMatcher = METHOD_ARG_PATTERN.toPattern().matcher(args)
            while (argMatcher.find()) {
                argTypes.add(argMatcher.group(1).toLowerCase().split("<")[0])
            }
            if (args.isNotEmpty() && argTypes.size == 0) {
                throw RuntimeException("Got non-empty parameters for method $functionName but couldn't parse them. Signature: \"$functionSig\"")
            }
        }
        fun matches(input: String, fuzzy: Boolean): Boolean {
            val matcher = METHOD_PATTERN.toPattern().matcher("ff $input")
            if (!matcher.find()) return false
            if (!matcher.group(2).equals(functionName, true)) return false
            if (fuzzy) return true
            val args: String = matcher.group(3)
            val split = args.toLowerCase().split(",".toRegex()).toTypedArray()
            val argLength = if (args.trim { it <= ' ' }.isEmpty()) 0 else split.size
            if (argLength != argTypes.size) return false
            for (i in argTypes.indices) {
                if (split[i].trim { it <= ' ' } != argTypes[i]) return false
            }
            return true
        }

        override val shortTitle = parent.className + '#' + functionName + '(' + parameters + ") : " + returnType

        override fun getUrl(jdocBase: String) = fixUrl(getLink(jdocBase, parent) + hashLink)

        override val title = annoPrefix + shortTitle

        private val annoPrefix: String
            get() {
                if (methodAnnos.isEmpty()) return ""
                var deprecated = false
                var deprecatedSince: String? = null
                var builder = StringBuilder()
                for (methodAnno in methodAnnos) {
                    when (methodAnno.name) {
                        "Deprecated" -> deprecated = true
                        "DeprecatedSince" -> deprecatedSince =
                            methodAnno.args.substring(2, methodAnno.args.length - 2)
                        else -> builder.append('@').append(methodAnno.toString()).append(' ')
                    }
                }
                if (deprecated || deprecatedSince != null) {
                    val tmp = StringBuilder("@Deprecated")
                    if (deprecatedSince != null) tmp.append("(Since ").append(deprecatedSince).append(") ")
                    builder = tmp.append(builder)
                }
                return builder.substring(0, builder.length - 1)
            }

        class MethodAnnotation(val name: String, val args: String) {
            override fun toString(): String {
                return name + args
            }
        }
    }

    class ValueDocumentation(
        val parent: ClassDocumentation,
        val name: String,
        private val hashLink: String?,
        private val sig: String,
        override val content: String
    ) :
        Documentation {
        override val title: String
            get() = if (parent.isEnum) shortTitle else parent.className + " - " + sig
        override val shortTitle: String
            get() = parent.className + '.' + name

        override fun getUrl(jdocBase: String): String {
            return getLink(jdocBase, parent) + hashLink
        }
    }
}
