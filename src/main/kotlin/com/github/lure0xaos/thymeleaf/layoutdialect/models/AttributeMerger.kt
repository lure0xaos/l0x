package com.github.lure0xaos.thymeleaf.layoutdialect.models

import com.github.lure0xaos.thymeleaf.layoutdialect.LayoutDialect
import com.github.lure0xaos.thymeleaf.layoutdialect.context.extensions.IContextExtensions.getPrefixForDialect
import com.github.lure0xaos.thymeleaf.layoutdialect.fragments.FragmentProcessor
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IAttributeExtensions.equalsName
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.asBoolean
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.first
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.IModel
import org.thymeleaf.model.IProcessableElementTag

class AttributeMerger(private val context: ITemplateContext) : ModelMerger {
    override fun merge(targetModel: IModel, sourceModel: IModel): IModel {
        val targetExists = asBoolean(targetModel)
        val sourceExists = asBoolean(sourceModel)
        if (!targetExists || !sourceExists) {
            if (targetExists && asBoolean(targetModel.cloneModel())) return targetModel.cloneModel()
            if (sourceExists) return sourceModel.cloneModel()
            throw IllegalArgumentException()
        }
        val mergedModel = targetModel.cloneModel()
        val layoutDialectPrefix = getPrefixForDialect(context, LayoutDialect::class.java)
        for (sourceAttribute in (first(sourceModel) as IProcessableElementTag).allAttributes) {
            if (equalsName(sourceAttribute, layoutDialectPrefix!!, FragmentProcessor.PROCESSOR_NAME)) {
                val mergedEvent = first(mergedModel) as IProcessableElementTag
                mergedModel.replace(
                    0, context.modelFactory.replaceAttribute(
                        mergedEvent,
                        sourceAttribute.attributeDefinition.attributeName, sourceAttribute.attributeCompleteName,
                        sourceAttribute.value
                    )
                )
            }
        }
        return mergedModel
    }
}
