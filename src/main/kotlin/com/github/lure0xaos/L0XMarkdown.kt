package com.github.lure0xaos

import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.Reference
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet
import java.net.URL
import java.nio.charset.Charset

class L0XMarkdown(
    private val charset: Charset,
    private val metaPrefix: String,
    config: MutableDataSet = MutableDataSet(),
    configure: (Parser.Builder, HtmlRenderer.Builder) -> Pair<Parser, HtmlRenderer> =
        { parserBuilder, rendererBuilder -> parserBuilder.build() to rendererBuilder.build() }
) {
    private val parser: Parser
    private val renderer: HtmlRenderer

    init {
        val (parser, renderer) = configure(
            Parser.Builder(config),
            HtmlRenderer.Builder(config.set(HtmlRenderer.SOFT_BREAK, HtmlRenderer.HARD_BREAK.defaultValue))
        )
        this.parser = parser
        this.renderer = renderer
    }

    fun loadDocument(input: URL): Document? = if (input.exists()) parser.parse(input.readFile(charset))
    else null

    fun render(node: Node?): String = if (node == null) ""
    else renderer.render(node)

    fun extractMeta(document: Document?): Map<String, String> = document?.extractAll(
        Reference::class.java,
        { it.reference.toString().startsWith(metaPrefix) },
        { it.reference.toString().substring(metaPrefix.length) },
        { it.title.toString() }) ?: mapOf()

    fun extractHeading(document: Document?): String = if (document == null) ""
    else document.extractFirst(Heading::class.java) { heading -> heading.text.toString() } ?: ""

    private fun <T> Document.extractAll(
        type: Class<T>,
        filter: (T) -> Boolean,
        keyMapper: (T) -> String,
        valueMapper: (T) -> String,
    ): Map<String, String> =
        this.children.filter { it.isOrDescendantOfType(type) }.map(type::cast).filter(filter)
            .associate { keyMapper(it) to valueMapper(it) }

    private fun <T> Document.extractFirst(type: Class<T>, mapper: (T) -> String): String? =
        this.children.filter { it.isOrDescendantOfType(type) }.map(type::cast).map { mapper(it) }.firstOrNull()
}
