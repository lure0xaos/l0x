package com.github.lure0xaos.thymeleaf.layoutdialect.decorators.html

import com.github.lure0xaos.thymeleaf.layoutdialect.decorators.Decorator
import com.github.lure0xaos.thymeleaf.layoutdialect.decorators.SortingStrategy
import com.github.lure0xaos.thymeleaf.layoutdialect.models.AttributeMerger
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.asBoolean
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.childModelIterator
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.findIndexOf
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.findModel
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.insertModelWithWhitespace
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.removeAllModels
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.ITemplateEventExtensions.isOpeningElementOf
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.IModel
import org.thymeleaf.model.IModelFactory

class HtmlHeadDecorator internal constructor(
    private val context: ITemplateContext,
    private val sortingStrategy: SortingStrategy
) : Decorator {
    override fun decorate(targetModel: IModel, sourceModel: IModel): IModel {
        require(!(!asBoolean(targetModel) && !asBoolean(sourceModel)))
        val modelFactory: IModelFactory = context.modelFactory
        val resultHeadModel: IModel = AttributeMerger(context).merge(targetModel, sourceModel)
        if (asBoolean(sourceModel) && asBoolean(targetModel)) {
            val iterator: Iterator<IModel> = childModelIterator(sourceModel)
            while (iterator.hasNext()) {
                val model: IModel = iterator.next()
                insertModelWithWhitespace(
                    resultHeadModel,
                    sortingStrategy.findPositionForModel(resultHeadModel, model), model, modelFactory
                )
            }
        }
        val indexOfTitle: Int =
            findIndexOf(resultHeadModel) { isOpeningElementOf(it, "title") }
        if (indexOfTitle == -1) {
            return resultHeadModel
        }
        removeAllModels(resultHeadModel) { isOpeningElementOf(it, "title") }
        val targetTitleModel: IModel? =
            if (asBoolean(targetModel)) findModel(targetModel) { isOpeningElementOf(it, "title") } else null
        val sourceTitleModel: IModel? =
            if (asBoolean(sourceModel)) findModel(sourceModel) { isOpeningElementOf(it, "title") } else null
        val resultTitle: IModel = HtmlTitleDecorator(context).decorate(targetTitleModel!!, sourceTitleModel!!)
        insertModelWithWhitespace(resultHeadModel, indexOfTitle, resultTitle, modelFactory)
        return resultHeadModel
    }
}
