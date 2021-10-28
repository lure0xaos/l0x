package com.github.lure0xaos.thymeleaf.layoutdialect.decorators.html

import com.github.lure0xaos.thymeleaf.layoutdialect.decorators.Decorator
import com.github.lure0xaos.thymeleaf.layoutdialect.models.AttributeMerger
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.asBoolean
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.IModel

class HtmlBodyDecorator internal constructor(private val context: ITemplateContext) : Decorator {
    override fun decorate(targetModel: IModel, sourceModel: IModel): IModel {
        if (asBoolean(targetModel) && asBoolean(sourceModel))
            return AttributeMerger(context).merge(targetModel, sourceModel)
        if (asBoolean(targetModel) && asBoolean(targetModel.cloneModel())) return targetModel.cloneModel()
        if (asBoolean(sourceModel)) return sourceModel.cloneModel()
        throw IllegalArgumentException()
    }
}
