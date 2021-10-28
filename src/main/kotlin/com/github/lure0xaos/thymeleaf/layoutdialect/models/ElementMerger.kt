package com.github.lure0xaos.thymeleaf.layoutdialect.models

import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.asBoolean
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.first
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.*

class ElementMerger(private val context: ITemplateContext) : ModelMerger {
    override fun merge(targetModel: IModel, sourceModel: IModel): IModel {
        val targetExists = asBoolean(targetModel)
        val sourceExists = asBoolean(sourceModel)
        if (!targetExists || !sourceExists) {
            if (targetExists && asBoolean(targetModel.cloneModel())) return targetModel.cloneModel()
            if (sourceExists) return sourceModel.cloneModel()
            throw IllegalArgumentException()
        }
        val modelFactory = context.modelFactory
        val sourceRootEvent = first(sourceModel)
        val sourceRootElement = modelFactory.createModel(sourceRootEvent)
        val targetRootEvent = first(targetModel)
        val event: ITemplateEvent? = if (sourceRootEvent is IOpenElementTag) modelFactory.createOpenElementTag(
            (sourceRootEvent as IElementTag).elementCompleteName,
            (targetRootEvent as IProcessableElementTag).attributeMap, AttributeValueQuotes.DOUBLE, false
        ) else {
            if (sourceRootEvent is IStandaloneElementTag) modelFactory.createStandaloneElementTag(
                (sourceRootEvent as IElementTag).elementCompleteName,
                (targetRootEvent as IProcessableElementTag).attributeMap, AttributeValueQuotes.DOUBLE, false,
                sourceRootEvent.isMinimized
            ) else null
        }
        val mergedModel = sourceModel.cloneModel()
        val first = first(AttributeMerger(context).merge(modelFactory.createModel(event), sourceRootElement))
        mergedModel.replace(0, first)
        return mergedModel
    }
}
