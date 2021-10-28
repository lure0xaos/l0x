package com.github.lure0xaos.thymeleaf.layoutdialect.fragments

import com.github.lure0xaos.thymeleaf.layoutdialect.fragments.FragmentParameterNamesExtractor.extract
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.first
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.IModel
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.IElementModelStructureHandler
import org.thymeleaf.standard.expression.FragmentExpression

class FragmentParameterVariableUpdater(private val dialectPrefix: String, private val context: ITemplateContext) {
    fun updateLocalVariables(
        fragmentExpression: FragmentExpression, fragment: IModel,
        structureHandler: IElementModelStructureHandler,
    ) {
        if (fragmentExpression.hasSyntheticParameters()) {
            val fragmentDefinition = (first(fragment) as IProcessableElementTag)
                .getAttributeValue(dialectPrefix, FragmentProcessor.PROCESSOR_NAME)
            val parameterNames = extract(fragmentDefinition)
            for ((i, item) in fragmentExpression.parameters.withIndex()) {
                structureHandler.setLocalVariable(parameterNames[i], item.right.execute(context))
            }
        } else {
            for (parameter in fragmentExpression.parameters) {
                structureHandler.setLocalVariable(
                    parameter.left.execute(context).toString(),
                    parameter.right.execute(context)
                )
            }
        }
    }
}
