package com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions

import org.thymeleaf.model.ICloseElementTag
import org.thymeleaf.model.IElementTag

object ICloseElementTagExtensions {
    fun equals(self: ICloseElementTag?, other: Any?): Boolean =
        other is ICloseElementTag && self?.elementDefinition == (other as IElementTag?)?.elementDefinition
}
