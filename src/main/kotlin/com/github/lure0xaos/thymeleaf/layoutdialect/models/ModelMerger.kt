package com.github.lure0xaos.thymeleaf.layoutdialect.models

import org.thymeleaf.model.IModel

fun interface ModelMerger {
    fun merge(targetModel: IModel, sourceModel: IModel): IModel
}
