package com.github.lure0xaos.thymeleaf.layoutdialect.decorators

import org.thymeleaf.model.IModel

fun interface SortingStrategy {
    fun findPositionForModel(headModel: IModel, childModel: IModel): Int
}
