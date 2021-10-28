package com.github.lure0xaos.thymeleaf.layoutdialect.decorators

import com.github.lure0xaos.thymeleaf.expressionprocessor.ExpressionProcessor
import com.github.lure0xaos.thymeleaf.layoutdialect.decorators.html.HtmlDocumentDecorator
import com.github.lure0xaos.thymeleaf.layoutdialect.decorators.xml.XmlDocumentDecorator
import com.github.lure0xaos.thymeleaf.layoutdialect.fragments.FragmentFinder
import com.github.lure0xaos.thymeleaf.layoutdialect.fragments.extensions.FragmentExtensions.setLocalFragmentCollection
import com.github.lure0xaos.thymeleaf.layoutdialect.models.TemplateModelFinder
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.find
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.findIndexOf
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.first
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.replaceModel
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IProcessableElementTagExtensions.equalsIgnoreXmlns
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IModel
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeModelProcessor
import org.thymeleaf.processor.element.IElementModelStructureHandler
import org.thymeleaf.templatemode.TemplateMode

class DecorateProcessor private constructor(
    templateMode: TemplateMode, dialectPrefix: String, private val sortingStrategy: SortingStrategy,
    private val autoHeadMerging: Boolean, attributeName: String,
) : AbstractAttributeModelProcessor(
    templateMode,
    dialectPrefix,
    null,
    false,
    attributeName,
    true,
    PROCESSOR_PRECEDENCE,
    false
) {
    constructor(
        templateMode: TemplateMode, dialectPrefix: String, sortingStrategy: SortingStrategy,
        autoHeadMerging: Boolean,
    ) : this(templateMode, dialectPrefix, sortingStrategy, autoHeadMerging, PROCESSOR_NAME)

    override fun doProcess(
        context: ITemplateContext, model: IModel, attributeName: AttributeName,
        attributeValue: String, structureHandler: IElementModelStructureHandler,
    ) {
        val templateModelFinder = TemplateModelFinder(context)
        val contentTemplateName = context.templateData.template
        val contentTemplate = templateModelFinder.findTemplate(contentTemplateName).cloneModel()
        val contentRootEvent =
            find(contentTemplate, IProcessableElementTag::class.java::isInstance) as IProcessableElementTag
        var rootElement = first(model) as IProcessableElementTag
        require(
            equalsIgnoreXmlns(
                contentRootEvent,
                rootElement
            )
        ) { "layout:decorate/data-layout-decorate must appear in the root element of your template" }
        if (rootElement.hasAttribute(attributeName)) {
            rootElement = context.modelFactory.removeAttribute(rootElement, attributeName)
            model.replace(0, rootElement)
        }
        replaceModel(
            contentTemplate,
            findIndexOf(contentTemplate, IProcessableElementTag::class.java::isInstance),
            model
        )
        val decorateTemplateExpression = ExpressionProcessor(context).parseFragmentExpression(attributeValue)
        val decorateTemplate0 = templateModelFinder.findTemplate(decorateTemplateExpression)
        val decorateTemplateData = decorateTemplate0.templateData
        val decorateTemplate = decorateTemplate0.cloneModel()
        val pageFragments: Map<String, List<IModel>> = FragmentFinder(dialectPrefix).findFragments(model)
        val decorator: Decorator = when (templateMode) {
            TemplateMode.HTML -> HtmlDocumentDecorator(context, sortingStrategy, autoHeadMerging)
            TemplateMode.XML -> XmlDocumentDecorator(context)
            else -> throw IllegalArgumentException("Layout dialect cannot be applied to the $templateMode template mode, only HTML and XML template modes are currently supported")
        }
        val resultTemplate = decorator.decorate(decorateTemplate, contentTemplate)
        replaceModel(model, 0, resultTemplate)
        structureHandler.setTemplateData(decorateTemplateData)
        setLocalFragmentCollection(structureHandler, context, pageFragments, true)
        if (decorateTemplateExpression.hasParameters()) {
            require(!decorateTemplateExpression.hasSyntheticParameters()) { "Fragment parameters must be named when used with layout:decorate/data-layout-decorate" }
            for (parameter in decorateTemplateExpression.parameters) {
                structureHandler.setLocalVariable(
                    parameter.left.execute(context).toString(),
                    parameter.right.execute(context)
                )
            }
        }
    }

    companion object {
        private const val PROCESSOR_NAME = "decorate"
        private const val PROCESSOR_PRECEDENCE = 0
    }
}
