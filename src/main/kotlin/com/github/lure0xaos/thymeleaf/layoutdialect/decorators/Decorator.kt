package com.github.lure0xaos.thymeleaf.layoutdialect.decorators

import org.thymeleaf.model.IModel

fun interface Decorator {
    fun decorate(targetModel: IModel, sourceModel: IModel): IModel
}
