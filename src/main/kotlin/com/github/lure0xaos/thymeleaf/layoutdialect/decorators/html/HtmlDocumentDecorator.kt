package com.github.lure0xaos.thymeleaf.layoutdialect.decorators.html

import com.github.lure0xaos.thymeleaf.layoutdialect.decorators.SortingStrategy
import com.github.lure0xaos.thymeleaf.layoutdialect.decorators.xml.XmlDocumentDecorator
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.asBoolean
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.findIndexOf
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.findIndexOfModel
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.findModel
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.insertModelWithWhitespace
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.replaceModel
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.ITemplateEventExtensions.isClosingElementOf
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.ITemplateEventExtensions.isOpeningElementOf
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.*

class HtmlDocumentDecorator(
    context: ITemplateContext,
    private val sortingStrategy: SortingStrategy,
    private val autoHeadMerging: Boolean
) : XmlDocumentDecorator(context) {
    override fun decorate(targetModel: IModel, sourceModel: IModel): IModel {
        val modelFactory: IModelFactory = context.modelFactory
        val resultDocumentModel: IModel = targetModel.cloneModel()
        if (autoHeadMerging) {
            val targetHeadModel: IModel =
                findModel(resultDocumentModel) { isOpeningElementOf(it, "head") }
            val resultHeadModel: IModel = HtmlHeadDecorator(context, sortingStrategy)
                .decorate(
                    targetHeadModel,
                    findModel(sourceModel) { isOpeningElementOf(it, "head") })
            if (asBoolean(resultHeadModel)) {
                if (asBoolean(targetHeadModel)) {
                    replaceModel(
                        resultDocumentModel,
                        findIndexOfModel(resultDocumentModel, targetHeadModel), resultHeadModel
                    )
                } else {
                    insertModelWithWhitespace(resultDocumentModel, findIndexOf(resultDocumentModel) {
                        it is IOpenElementTag && (it as IElementTag).elementCompleteName == "body" ||
                                it is ICloseElementTag && (it as IElementTag).elementCompleteName == "html"
                    } - 1, resultHeadModel, modelFactory)
                }
            }
        } else {
            // TODO: If autoHeadMerging is false, this really shouldn't be needed as
            //       the basis for `resultDocumentModel` should be the source model.
            //       This 'hack' is OK for an experimental option, but the fact that
            //       it exists means I should rethink how the result model is made.
            replaceModel(resultDocumentModel,
                findIndexOf(resultDocumentModel) { isOpeningElementOf(it, "head") },
                findModel(sourceModel) { isOpeningElementOf(it, "head") }
            )
        }
        val targetBodyModel: IModel =
            findModel(resultDocumentModel) { it is IOpenElementTag && (it as IElementTag).elementCompleteName == "body" }
        val resultBodyModel: IModel = HtmlBodyDecorator(context).decorate(targetBodyModel,
            findModel(sourceModel) { it is IOpenElementTag && (it as IElementTag).elementCompleteName == "body" }
        )
        if (asBoolean(resultBodyModel)) {
            if (asBoolean(targetBodyModel)) {
                replaceModel(
                    resultDocumentModel,
                    findIndexOfModel(resultDocumentModel, targetBodyModel),
                    resultBodyModel
                )
            } else {
                insertModelWithWhitespace(
                    resultDocumentModel,
                    findIndexOf(resultDocumentModel) { isClosingElementOf(it, "html") } - 1,
                    resultBodyModel,
                    modelFactory)
            }
        }
        return super.decorate(resultDocumentModel, sourceModel)
    }
}
