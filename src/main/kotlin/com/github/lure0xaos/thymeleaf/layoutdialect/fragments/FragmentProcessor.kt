package com.github.lure0xaos.thymeleaf.layoutdialect.fragments

import com.github.lure0xaos.thymeleaf.layoutdialect.fragments.extensions.FragmentExtensions.getFragmentCollection
import com.github.lure0xaos.thymeleaf.layoutdialect.models.ElementMerger
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.first
import org.slf4j.LoggerFactory
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IModel
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.templatemode.TemplateMode

class FragmentProcessor(templateMode: TemplateMode?, dialectPrefix: String?) : AbstractAttributeTagProcessor(
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
        if (templateMode == TemplateMode.HTML) {
            for (element in context.elementStack) {
                if (element.elementCompleteName == "head") {
                    if (!warned) {
                        logger.warn("You don't need to put the layout:fragment/data-layout-fragment attribute into the <head> section - the decoration process will automatically copy the <head> section of your content templates into your layout page.")
                        warned = true
                    }
                    break
                }
            }
        }
        val fragments: List<IModel>? = getFragmentCollection(context, false)[attributeValue]
        if (!fragments.isNullOrEmpty()) {
            val fragment = fragments[fragments.size - 1]
            val modelFactory = context.modelFactory
            val replacementModel = ElementMerger(context).merge(modelFactory.createModel(tag), fragment)
            replacementModel.replace(
                0, modelFactory.removeAttribute(
                    first(replacementModel) as IProcessableElementTag,
                    dialectPrefix, PROCESSOR_NAME
                )
            )
            structureHandler.replaceWith(replacementModel, true)
        }
    }

    companion object {
        const val PROCESSOR_NAME: String = "fragment"
        private const val PROCESSOR_PRECEDENCE = 1
        private val logger = LoggerFactory.getLogger(FragmentProcessor::class.java)
        private var warned = false
    }
}
