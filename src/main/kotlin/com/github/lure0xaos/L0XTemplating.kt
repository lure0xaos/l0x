package com.github.lure0xaos

import com.github.lure0xaos.thymeleaf.layoutdialect.LayoutDialect
import com.github.lure0xaos.thymeleaf.layoutdialect.decorators.strategies.GroupingStrategy
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect
import org.thymeleaf.messageresolver.StandardMessageResolver
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.UrlTemplateResolver
import java.net.URL
import java.nio.charset.Charset
import java.util.*

class L0XTemplating(
    private val locale: Locale,
    charset: Charset,
    messages: ResourceBundle,
    private val symbols: Map<String, Any?>,
    prefix: URL,
    suffix: String,
    configure: (TemplateEngine) -> Unit = {}
) {
    private val engine: TemplateEngine = TemplateEngine()

    fun process(template: String, variables: Map<String, Any?>): String =
        engine.process(template, Context(locale, symbols + variables))

    init {
        engine.addDialect(LayoutDialect(GroupingStrategy()))
        engine.addDialect(Java8TimeDialect())
        val messageResolver = StandardMessageResolver()
        messageResolver.defaultMessages = messages.toProperties()
        engine.setMessageResolver(messageResolver)
        val templateResolver = UrlTemplateResolver()
        templateResolver.templateMode = TemplateMode.HTML
        templateResolver.characterEncoding = charset.name()
        templateResolver.prefix = "$prefix/"
        templateResolver.suffix = suffix
        engine.setTemplateResolver(templateResolver)
        configure(engine)
    }

}
