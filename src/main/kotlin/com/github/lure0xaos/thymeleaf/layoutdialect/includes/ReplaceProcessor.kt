package com.github.lure0xaos.thymeleaf.layoutdialect.includes

import com.github.lure0xaos.thymeleaf.expressionprocessor.ExpressionProcessor
import com.github.lure0xaos.thymeleaf.layoutdialect.fragments.FragmentFinder
import com.github.lure0xaos.thymeleaf.layoutdialect.fragments.FragmentParameterVariableUpdater
import com.github.lure0xaos.thymeleaf.layoutdialect.fragments.extensions.FragmentExtensions.setLocalFragmentCollection
import com.github.lure0xaos.thymeleaf.layoutdialect.models.TemplateModelFinder
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.replaceModel
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IModel
import org.thymeleaf.processor.element.AbstractAttributeModelProcessor
import org.thymeleaf.processor.element.IElementModelStructureHandler
import org.thymeleaf.templatemode.TemplateMode

class ReplaceProcessor(templateMode: TemplateMode?, dialectPrefix: String?) : AbstractAttributeModelProcessor(
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
        val fragmentForReplacement = TemplateModelFinder(context).findFragment(fragmentExpression)
        val replaceFragments: Map<String, List<IModel>> = FragmentFinder(dialectPrefix).findFragments(model)
        setLocalFragmentCollection(structureHandler, context, replaceFragments, false)
        structureHandler.setTemplateData(fragmentForReplacement.templateData)
        val fragmentForReplacementUse = fragmentForReplacement.cloneModel()
        replaceModel(model, 0, fragmentForReplacementUse)
        FragmentParameterVariableUpdater(dialectPrefix, context)
            .updateLocalVariables(fragmentExpression, fragmentForReplacementUse, structureHandler)
    }

    companion object {
        private const val PROCESSOR_NAME = "replace"
        private const val PROCESSOR_PRECEDENCE = 0
    }
}
