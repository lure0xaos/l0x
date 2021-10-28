package com.github.lure0xaos

import com.github.lure0xaos.L0XMenuItem.Companion.buildBreadcrumb
import com.github.lure0xaos.L0XMenuItem.Companion.buildMenu
import com.github.lure0xaos.L0XResources.getContextURL
import com.github.lure0xaos.L0XResources.getRootPath
import com.github.lure0xaos.L0XResources.readMap
import com.github.lure0xaos.L0XResources.readResourceBundle
import com.github.lure0xaos.L0XResources.resolveContext
import com.github.lure0xaos.L0XResources.resolveFile
import com.github.lure0xaos.log.L0XLog
import com.github.lure0xaos.log.L0XLog.logging
import com.vladsch.flexmark.util.ast.Document
import java.awt.Desktop
import java.awt.GraphicsEnvironment
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private const val ARG_CHARSET = "charset"
private const val ARG_LOCALE = "locale"
private const val ARG_OUT = "out"
private const val ARG_OPEN = "open"
private const val RES_CONFIG: String = "/config"
private const val RES_MESSAGES: String = "/messages"
private const val RES_SYMBOLS: String = "/symbols"
private const val PATH_TEMPLATES: String = "/templates"
private const val PATH_STATIC: String = "/static"
private const val PATH_TOPICS: String = "/topics"
private const val PATH_CONTENT: String = "/content"
private const val PREFIX_STATIC_CSS: String = "/css/"
private const val PREFIX_STATIC_JS: String = "/js/"
private const val EXT_TEMPLATE: String = ".html"
private const val EXT_HTML: String = ".html"
private const val EXT_TOPICS: String = ".md"
private const val EXT_CONTENT: String = ".md"
private const val EXT_DATA: String = ".properties"
private const val EXT_CSS: String = ".css"
private const val EXT_JS: String = ".js"
private const val KEY_TOPIC: String = "topic"
private const val KEY_LINK_CSS: String = "linkCss"
private const val KEY_LINK_JS: String = "linkJs"
private const val KEY_MENU: String = "menu"
private const val KEY_PAGE: String = "page"
private const val KEY_PAGE_DATA: String = "pageData"
private const val KEY_PAGES: String = "pages"
private const val KEY_CONTENT: String = "content"
private const val KEY_META: String = "meta"
private const val KEY_META_GENRE: String = "genre"
private const val KEY_META_DATE: String = "date"
private const val KEY_META_AUTHOR: String = "author"
private const val KEY_BREADCRUMB: String = "breadcrumb"
private const val KEY_TOPIC_ALIAS: String = "alias"
private const val KEY_TOPIC_FILE: String = "file"
private const val KEY_TOPIC_HEADING: String = "heading"
private const val KEY_TOPIC_CONTENT: String = "content"
private const val KEY_TOPIC_AUTHOR: String = "author"
private const val KEY_TOPIC_AUTHOR_KEY: String = "authorKey"
private const val KEY_TOPIC_DATE: String = "date"
private const val KEY_TOPIC_YEAR: String = "year"
private const val KEY_TOPIC_GENRE_KEY: String = "genreKey"
private const val KEY_TOPIC_GENRE: String = "genre"
private const val TEMPLATE_LAYOUT: String = "layout"
private const val TEMPLATE_TOPIC: String = "topic"
private const val TEMPLATE_INDEX: String = "index"
private const val TOPIC_PREFIX: String = "topic."
private const val GENRE_PREFIX: String = "genre."
private const val META: String = "@"
private const val FORMAT_DATE_TIME: String = "yyyy-MM-dd HH:mm"
private const val OUT_STATIC: String = "static"
private const val OUT_INDEX: String = "index"
private const val OUT_ROOT: String = "out"
private const val DEFAULT_LOCALE: String = "ru"

class L0X(private val charset: Charset, private val locale: Locale, private val out: Path, private val open: Boolean) {
    private val messages: ResourceBundle = readResourceBundle(RES_MESSAGES, charset, locale)
    private val templating: L0XTemplating = L0XTemplating(
        locale, charset, messages,
        symbols = readMap(RES_SYMBOLS, charset, locale),
        prefix = getContextURL(PATH_TEMPLATES, locale)!!,
        suffix = EXT_TEMPLATE
    )
    private val markdown: L0XMarkdown = L0XMarkdown(charset, metaPrefix = META)
    private val dateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT_DATE_TIME, locale)

    fun start() {
        {
            out.recreateFolder()
            val topics: List<Map<String, String>> = loadTopics()
            val pages: Map<String, String> = loadPages()
            val menu: Set<L0XMenuItem> = buildMenu(hierarchy, topics, selected = null)
            val index: Path = processIndex(pages, menu)
            processTopics(hierarchy, pages, topics)
            processPages(pages, menu)
            ;{
            getContextURL(PATH_STATIC, locale)?.copyDirectory(out.resolve(OUT_STATIC))
        }.logging(System.Logger.Level.INFO, "copying static... $PATH_STATIC")
            L0XLog.info("all done. $index")
            if (open) {
                out.open()
                index.open()
            }
        }.logging(System.Logger.Level.INFO, "")
    }

    private fun processIndex(pages: Map<String, String>, menu: Set<L0XMenuItem>): Path = {
        loadDocument(PATH_CONTENT, TEMPLATE_INDEX, EXT_CONTENT).let {
            writeTemplate(
                template = TEMPLATE_INDEX, file = OUT_INDEX + EXT_HTML, vars = mapOf(
                    KEY_PAGES to pages,
                    KEY_MENU to menu,
                    KEY_PAGE_DATA to loadPage(TEMPLATE_INDEX),
                    KEY_META to markdown.extractMeta(it),
                    KEY_CONTENT to markdown.render(it),
                    KEY_LINK_CSS to resolveFile(PATH_STATIC, PREFIX_STATIC_CSS + TEMPLATE_INDEX + EXT_CSS, locale),
                    KEY_LINK_JS to resolveFile(PATH_STATIC, PREFIX_STATIC_JS + TEMPLATE_INDEX + EXT_JS, locale),
                )
            )
        }
    }.logging(System.Logger.Level.INFO, "$OUT_INDEX [0/0] $TEMPLATE_INDEX")

    private fun processTopics(
        hierarchy: List<Pair<String, String>>,
        pages: Map<String, String>,
        topics: List<Map<String, String>>,
    ) =
        topics.forEachIndexed { index, topic ->
            {
                writeTemplate(
                    template = TEMPLATE_TOPIC, file = topic.getValue(KEY_TOPIC_FILE), vars = mapOf(
                        KEY_PAGES to pages,
                        KEY_MENU to buildMenu(hierarchy, topics, selected = topic),
                        KEY_BREADCRUMB to buildBreadcrumb(hierarchy, topic),
                        KEY_TOPIC to topic,
                        KEY_LINK_CSS to resolveFile(PATH_STATIC, PREFIX_STATIC_CSS + TEMPLATE_TOPIC + EXT_CSS, locale),
                        KEY_LINK_JS to resolveFile(PATH_STATIC, PREFIX_STATIC_JS + TEMPLATE_TOPIC + EXT_JS, locale),
                    )
                )
            }.logging(System.Logger.Level.INFO, "$KEY_TOPIC [${index + 1}/${topics.size}] ${topic[KEY_TOPIC_FILE]}")
        }

    private fun processPages(pages: Map<String, String>, menu: Set<L0XMenuItem>) {
        pages.forEachIndexed { index, (page, file) ->
            {
                loadDocument(PATH_CONTENT, page, EXT_CONTENT)?.also {
                    writeTemplate(
                        template = page, file = file, vars = mapOf(
                            KEY_PAGES to pages,
                            KEY_MENU to menu,
                            KEY_PAGE to page,
                            KEY_PAGE_DATA to loadPage(page),
                            KEY_META to markdown.extractMeta(it),
                            KEY_CONTENT to markdown.render(it),
                            KEY_LINK_CSS to resolveFile(PATH_STATIC, PREFIX_STATIC_CSS + page + EXT_CSS, locale),
                            KEY_LINK_JS to resolveFile(PATH_STATIC, PREFIX_STATIC_JS + page + EXT_JS, locale),
                        )
                    )
                }
            }.logging(System.Logger.Level.INFO, "$KEY_PAGE [${index + 1}/${pages.size}] $file")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadPage(page: String): Map<String, Any?> {
        val resource = resolveContext(PATH_CONTENT, page + EXT_DATA, locale)
        if (!resource.exists()) return mapOf()
        val properties = resource.readMap(charset)
        val map: MutableMap<String, Any?> = sortedMapOf()
        properties.forEach { (key, value) ->
            val split = key.split('.')
            var partMap: MutableMap<String, Any?> = map
            split.forEachIndexed { index, part ->
                val lastIndex = split.lastIndex
                when (index) {
                    lastIndex -> {
                        partMap[part] = value
                    }

                    else -> {
                        if (partMap[part] == null) {
                            val mapOf = sortedMapOf<String, Any?>()
                            partMap[part] = mapOf
                            partMap = mapOf
                        } else {
                            partMap = partMap[part] as MutableMap<String, Any?>
                        }
                    }
                }
            }
        }
        return map
    }

    private fun loadTopics(): List<Map<String, String>> = {
        listFiles(PATH_TOPICS, EXT_TOPICS).map { alias ->
            val document: Document? = loadDocument(PATH_TOPICS, alias, EXT_TOPICS)
            val meta = markdown.extractMeta(document)
            meta + mapOf(
                KEY_TOPIC_ALIAS to alias,
                KEY_TOPIC_FILE to TOPIC_PREFIX + alias + EXT_HTML,
                KEY_TOPIC_HEADING to markdown.extractHeading(document),
                KEY_TOPIC_CONTENT to markdown.render(document),
                KEY_TOPIC_AUTHOR to meta.getValue(KEY_META_AUTHOR),
                KEY_TOPIC_AUTHOR_KEY to meta.getValue(KEY_META_AUTHOR).lowercase(locale),
                KEY_TOPIC_DATE to dateTimeFormatter.format(
                    LocalDateTime.parse(
                        meta.getValue(KEY_META_DATE),
                        dateTimeFormatter
                    )
                ),
                KEY_TOPIC_YEAR to LocalDateTime.parse(meta.getValue(KEY_META_DATE), dateTimeFormatter).year.toString(),
                KEY_TOPIC_GENRE_KEY to meta.getValue(KEY_META_GENRE),
                KEY_TOPIC_GENRE to messages.getString("$GENRE_PREFIX${meta.getValue(KEY_META_GENRE)}"),
            )
        }
    }.logging(System.Logger.Level.INFO, "loading topics... $PATH_TOPICS")

    private fun loadPages(): Map<String, String> = {
        listFiles(PATH_TEMPLATES, EXT_TEMPLATE)
            .filter { it != TEMPLATE_TOPIC && it != TEMPLATE_INDEX && it != TEMPLATE_LAYOUT }
            .associateWith { it + EXT_HTML }
    }.logging(System.Logger.Level.INFO, "loading pages... $KEY_PAGES")

    private fun listFiles(contextDirectory: String, extension: String): List<String> =
        getContextURL(contextDirectory, locale)!!.listDirectory { path, directory ->
            !directory && path.hasExtension(extension)
        }.map(URL::getBaseName)

    private fun loadDocument(contextDirectory: String, name: String, extension: String): Document? =
        markdown.loadDocument(resolveContext(contextDirectory, name + extension, locale))

    private fun writeTemplate(template: String, file: String, vars: Map<String, Any?>): Path =
        out.resolve(file).writeFile(templating.process(template, vars), charset)

    companion object {
        const val KEY_TOPIC_FILE: String = com.github.lure0xaos.KEY_TOPIC_FILE
        val hierarchy: List<Pair<String, String>> = listOf(
            KEY_TOPIC_AUTHOR_KEY to KEY_TOPIC_AUTHOR,
            KEY_TOPIC_YEAR to KEY_TOPIC_YEAR,
            KEY_TOPIC_GENRE_KEY to KEY_TOPIC_GENRE,
            KEY_TOPIC_ALIAS to KEY_TOPIC_HEADING
        )

        @JvmStatic
        fun main(args: Array<String>) {
            (args(args) + readMap(RES_CONFIG)).let {
                L0X(
                    charset = charset(it[ARG_CHARSET] ?: StandardCharsets.UTF_8.name()),
                    locale = Locale(it[ARG_LOCALE] ?: DEFAULT_LOCALE),
                    out = getRootPath().resolve(it[ARG_OUT] ?: OUT_ROOT),
                    open = it[ARG_OPEN].toBoolean()
                ).start()
            }
        }

        private fun args(args: Array<String>): Map<String, String> =
            args.filter { it.contains('=') }.associate { line: String ->
                line.substringBefore('=').trim() to
                        line.substringAfter('=').trim()
                            .removeSurrounding("\"")
                            .removeSurrounding("\'")
            }

        private fun Path.open() = try {
            if (Desktop.isDesktopSupported() && !GraphicsEnvironment.isHeadless()) {
                Desktop.getDesktop().open(toFile())
            } else {
                L0XLog.info("cannot open $this")
                Unit
            }
        } catch (e: Exception) {
            L0XLog.info("cannot open $this")
            Unit
        }

        private fun <K, V> Map<K, V>.forEachIndexed(action: (index: Int, Map.Entry<K, V>) -> Unit) {
            var index = 0
            this.forEach { element ->
                action(index, element)
                index++
            }
        }
    }
}
