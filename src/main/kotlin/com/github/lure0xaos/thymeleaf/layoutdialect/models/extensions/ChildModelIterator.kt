package com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions

import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.getModel
import org.thymeleaf.model.IModel

internal class ChildModelIterator(val parent: IModel) : Iterator<IModel> {
    private var currentIndex = 1
    override fun hasNext(): Boolean = currentIndex < parent.size() - 1

    override fun next(): IModel {
        val subModel = getModel(parent, currentIndex)
        currentIndex += subModel.size()
        return subModel
    }
}
