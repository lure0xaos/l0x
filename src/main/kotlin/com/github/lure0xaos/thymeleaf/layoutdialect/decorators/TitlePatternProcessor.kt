package com.github.lure0xaos.thymeleaf.layoutdialect.decorators

import com.github.lure0xaos.thymeleaf.layoutdialect.context.extensions.IContextExtensions.getAt
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.asBoolean
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IModel
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.templatemode.TemplateMode
import java.util.regex.Pattern

class TitlePatternProcessor(templateMode: TemplateMode?, dialectPrefix: String?) : AbstractAttributeTagProcessor(
    templateMode,
    dialectPrefix,
    null,
    false,
    PROCESSOR_NAME,
    true,
    PROCESSOR_PRECEDENCE,
    true
) {
    override fun doProcess(
        context: ITemplateContext, tag: IProcessableElementTag,
        attributeName: AttributeName, attributeValue: String, structureHandler: IElementTagStructureHandler,
    ) {
        require(tag.elementCompleteName == "title") { "$attributeName processor should only appear in a <title> element" }
        val modelFactory = context.modelFactory
        val contentTitle = getAt<IModel>(context, CONTENT_TITLE_KEY)
        val layoutTitle = getAt<IModel>(context, LAYOUT_TITLE_KEY)
        val titleModel = modelFactory.createModel()
        if (asBoolean(layoutTitle) && asBoolean(contentTitle)) {
            val matcher = TOKEN_PATTERN.matcher(attributeValue)
            while (matcher.find()) {
                val text = attributeValue.substring(matcher.regionStart(), matcher.start())
                if (text.isNotEmpty()) titleModel.add(modelFactory.createText(text))
                val token = matcher.group(1)
                if (token == TOKEN_LAYOUT_TITLE) titleModel.addModel(layoutTitle) else titleModel.addModel(contentTitle)
                matcher.region(matcher.regionStart() + text.length + token.length, attributeValue.length)
            }
            val remainingText = attributeValue.substring(matcher.regionStart())
            if (remainingText.isNotEmpty()) titleModel.add(modelFactory.createText(remainingText))
        } else if (asBoolean(contentTitle)) {
            titleModel.addModel(contentTitle)
        } else if (asBoolean(layoutTitle)) {
            titleModel.addModel(layoutTitle)
        }
        structureHandler.setBody(titleModel, true)
    }

    companion object {
        const val CONTENT_TITLE_KEY: String = "LayoutDialect::ContentTitle"
        const val LAYOUT_TITLE_KEY: String = "LayoutDialect::LayoutTitle"
        const val PROCESSOR_NAME: String = "title-pattern"
        private const val PROCESSOR_PRECEDENCE = 1
        private const val TOKEN_LAYOUT_TITLE = "\$LAYOUT_TITLE"
        private val TOKEN_PATTERN = Pattern.compile("(\\$(LAYOUT|CONTENT)_TITLE)")
    }
}
