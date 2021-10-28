package com.github.lure0xaos.thymeleaf.layoutdialect.includes

import com.github.lure0xaos.thymeleaf.expressionprocessor.ExpressionProcessor
import com.github.lure0xaos.thymeleaf.layoutdialect.fragments.FragmentFinder
import com.github.lure0xaos.thymeleaf.layoutdialect.fragments.FragmentParameterVariableUpdater
import com.github.lure0xaos.thymeleaf.layoutdialect.fragments.extensions.FragmentExtensions.setLocalFragmentCollection
import com.github.lure0xaos.thymeleaf.layoutdialect.models.TemplateModelFinder
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.removeChildren
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IModel
import org.thymeleaf.processor.element.AbstractAttributeModelProcessor
import org.thymeleaf.processor.element.IElementModelStructureHandler
import org.thymeleaf.templatemode.TemplateMode

class InsertProcessor(templateMode: TemplateMode?, dialectPrefix: String?) : AbstractAttributeModelProcessor(
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
        context: ITemplateContext, model: IModel, attributeName: AttributeName,
        attributeValue: String, structureHandler: IElementModelStructureHandler,
    ) {
        val fragmentExpression = ExpressionProcessor(context).parseFragmentExpression(attributeValue)
        val fragmentForInsertion = TemplateModelFinder(context).findFragment(fragmentExpression)
        val insertFragments: Map<String, List<IModel>> = FragmentFinder(dialectPrefix).findFragments(model)
        setLocalFragmentCollection(structureHandler, context, insertFragments, false)
        structureHandler.setTemplateData(fragmentForInsertion.templateData)
        val fragmentForInsertionUse = fragmentForInsertion.cloneModel()
        removeChildren(model)
        model.insertModel(1, fragmentForInsertionUse)
        FragmentParameterVariableUpdater(dialectPrefix, context)
            .updateLocalVariables(fragmentExpression, fragmentForInsertionUse, structureHandler)
    }

    companion object {
        private const val PROCESSOR_NAME = "insert"
        private const val PROCESSOR_PRECEDENCE = 0
    }
}
