package com.github.lure0xaos.thymeleaf.layoutdialect.decorators.html

import com.github.lure0xaos.thymeleaf.layoutdialect.LayoutDialect
import com.github.lure0xaos.thymeleaf.layoutdialect.context.extensions.IContextExtensions.getPrefixForDialect
import com.github.lure0xaos.thymeleaf.layoutdialect.context.extensions.IContextExtensions.putAt
import com.github.lure0xaos.thymeleaf.layoutdialect.decorators.Decorator
import com.github.lure0xaos.thymeleaf.layoutdialect.decorators.TitlePatternProcessor
import com.github.lure0xaos.thymeleaf.layoutdialect.models.ElementMerger
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.asBoolean
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.childModelIterator
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.first
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.IAttribute
import org.thymeleaf.model.IModel
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.standard.StandardDialect
import org.thymeleaf.standard.processor.StandardBlockTagProcessor
import org.thymeleaf.standard.processor.StandardTextTagProcessor
import org.thymeleaf.standard.processor.StandardUtextTagProcessor

class HtmlTitleDecorator internal constructor(private val context: ITemplateContext) : Decorator {
    override fun decorate(targetModel: IModel, sourceModel: IModel): IModel {
        val layoutDialectPrefix: String? = getPrefixForDialect(context, LayoutDialect::class.java)
        val standardDialectPrefix: String? = getPrefixForDialect(context, StandardDialect::class.java)
        val titlePatternProcessorRetriever: (IModel) -> IAttribute? = { titleModel: IModel ->
            require(asBoolean(titleModel))
            (first(titleModel) as IProcessableElementTag)
                .getAttribute(layoutDialectPrefix, TitlePatternProcessor.PROCESSOR_NAME)
        }
        var result: IAttribute? = null
        val attribute: IAttribute? = titlePatternProcessorRetriever(sourceModel)
        if (attribute != null) result = attribute
        if (result == null) result = titlePatternProcessorRetriever(targetModel)
        val titlePatternProcessor: IAttribute? = result
        return if (titlePatternProcessor != null) {
            val extractTitle: (IModel, String) -> Unit = { titleModel: IModel, contextKey: String ->
                if (!context.containsVariable(contextKey)) {
                    if (asBoolean(titleModel)) {
                        val titleTag: IProcessableElementTag = first(titleModel) as IProcessableElementTag
                        if (titleTag.hasAttribute(standardDialectPrefix, StandardTextTagProcessor.ATTR_NAME)) {
                            putAt(
                                context, contextKey,
                                build(
                                    standardDialectPrefix + ":" + StandardBlockTagProcessor.ELEMENT_NAME,
                                    standardDialectPrefix + ":" + StandardTextTagProcessor.ATTR_NAME,
                                    titleTag.getAttributeValue(
                                        standardDialectPrefix,
                                        StandardTextTagProcessor.ATTR_NAME
                                    )
                                )
                            )
                        } else if (titleTag.hasAttribute(standardDialectPrefix, StandardUtextTagProcessor.ATTR_NAME)) {
                            putAt(
                                context, contextKey,
                                build(
                                    standardDialectPrefix + ":" + StandardBlockTagProcessor.ELEMENT_NAME,
                                    standardDialectPrefix + ":" + StandardUtextTagProcessor.ATTR_NAME,
                                    titleTag.getAttributeValue(
                                        standardDialectPrefix,
                                        StandardUtextTagProcessor.ATTR_NAME
                                    )
                                )
                            )
                        } else {
                            val titleChildrenModel: IModel = context.modelFactory.createModel()
                            val iterator: Iterator<IModel> = childModelIterator(titleModel)
                            while (iterator.hasNext()) {
                                val model: IModel = iterator.next()
                                titleChildrenModel.addModel(model)
                            }
                            putAt(context, contextKey, titleChildrenModel)
                        }
                    }
                }
            }
            extractTitle(sourceModel, TitlePatternProcessor.CONTENT_TITLE_KEY)
            extractTitle(targetModel, TitlePatternProcessor.LAYOUT_TITLE_KEY)
            build("title", titlePatternProcessor.attributeCompleteName, titlePatternProcessor.value)
        } else {
            ElementMerger(context).merge(targetModel, sourceModel)
        }
    }

    private fun build(title: String, name: String, value: String): IModel {
        val model: IModel = context.modelFactory.createModel()
        model.add(context.modelFactory.createOpenElementTag(title, name, value))
        model.add(context.modelFactory.createCloseElementTag(title, false, false))
        return model
    }
}
