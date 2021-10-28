package com.github.lure0xaos.thymeleaf.layoutdialect.decorators.xml

import com.github.lure0xaos.thymeleaf.layoutdialect.decorators.Decorator
import com.github.lure0xaos.thymeleaf.layoutdialect.models.AttributeMerger
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.findModel
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.insertWithWhitespace
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.*

open class XmlDocumentDecorator(protected val context: ITemplateContext) : Decorator {
    override fun decorate(targetModel: IModel, sourceModel: IModel): IModel {
        val modelFactory = context.modelFactory
        val resultDocumentModel = AttributeMerger(context).merge(
            findModel(targetModel, IProcessableElementTag::class.java::isInstance),
            findModel(sourceModel, IProcessableElementTag::class.java::isInstance)
        )
        for (i in 0 until targetModel.size()) {
            val targetEvent = targetModel[i]
            if (targetEvent is IDocType) {
                var insert = true
                for (j in 0 until sourceModel.size()) {
                    val sourceEvent = sourceModel[j]
                    if (sourceEvent is IDocType) {
                        insert = false
                        break
                    }
                    if (sourceEvent is IOpenElementTag) {
                        break
                    }
                }
                if (insert) {
                    insertWithWhitespace(resultDocumentModel, 0, targetEvent, modelFactory)
                }
            } else if (targetEvent is IComment) {
                insertWithWhitespace(resultDocumentModel, 0, targetEvent, modelFactory)
            } else if (targetEvent is IOpenElementTag) {
                break
            }
        }
        for (i in targetModel.size() - 1 downTo 0) {
            val event = targetModel[i]
            if (event is IComment) {
                insertWithWhitespace(resultDocumentModel, resultDocumentModel.size(), event, modelFactory)
            } else if (event is ICloseElementTag) {
                break
            }
        }
        return resultDocumentModel
    }
}
