package com.github.lure0xaos.thymeleaf.layoutdialect.decorators.strategies

import com.github.lure0xaos.thymeleaf.layoutdialect.decorators.SortingStrategy
import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.isWhitespace
import org.thymeleaf.model.IModel

class AppendingStrategy : SortingStrategy {
    override fun findPositionForModel(headModel: IModel, childModel: IModel): Int {
        if (isWhitespace(childModel)) return -1
        val positions: Int = headModel.size()
        return if (positions > 2) positions - 2 else positions - 1
    }
}
