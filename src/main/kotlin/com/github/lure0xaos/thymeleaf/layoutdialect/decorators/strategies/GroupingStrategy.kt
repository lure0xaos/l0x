package com.github.lure0xaos.thymeleaf.layoutdialect.decorators.strategies

import com.github.lure0xaos.thymeleaf.layoutdialect.decorators.SortingStrategy
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.asBoolean
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.findIndexOfModel
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.first
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.isWhitespace
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.reversedChildModelIterator
import org.thymeleaf.model.*

@Suppress("unused")
class GroupingStrategy : SortingStrategy {
    override fun findPositionForModel(headModel: IModel, childModel: IModel): Int {
        if (isWhitespace(childModel)) {
            return -1
        }
        if (findIndexOfModel(headModel, childModel) != -1) {
            return -1
        }
        val type: HeadEventTypes? = HeadEventTypes.findMatchingType(childModel)
        var matchingModel: IModel? = null
        val iterator: Iterator<IModel> = reversedChildModelIterator(headModel)
        while (iterator.hasNext()) {
            val next: IModel = iterator.next()
            if (type == HeadEventTypes.findMatchingType(next)) {
                matchingModel = next
                break
            }
        }
        if (asBoolean(matchingModel)) return findIndexOfModel(headModel, matchingModel!!) + matchingModel.size()
        val positions: Int = headModel.size()
        return if (positions > 2) positions - 2 else positions - 1
    }

    private enum class HeadEventTypes(private val determinant: (ITemplateEvent) -> Boolean) {
        COMMENT(IComment::class.java::isInstance),
        META({ it is IProcessableElementTag && (it as IElementTag).elementCompleteName == "meta" }),
        SCRIPT({ it is IOpenElementTag && (it as IElementTag).elementCompleteName == "script" }),
        STYLE({ it is IOpenElementTag && (it as IElementTag).elementCompleteName == "style" }),
        STYLESHEET({
            it is IProcessableElementTag && (it as IElementTag).elementCompleteName == "link" &&
                    it.getAttributeValue("rel") == "stylesheet"
        }),
        TITLE({ it is IOpenElementTag && (it as IElementTag).elementCompleteName == "title" }),
        OTHER(IElementTag::class.java::isInstance);

        companion object {
            fun findMatchingType(model: IModel): HeadEventTypes? = values().firstOrNull { it.determinant(first(model)) }
        }
    }
}
