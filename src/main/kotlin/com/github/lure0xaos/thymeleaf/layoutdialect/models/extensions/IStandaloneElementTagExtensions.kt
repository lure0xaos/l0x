package com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions

import org.thymeleaf.model.IElementTag
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.model.IStandaloneElementTag

object IStandaloneElementTagExtensions {
    fun equals(self: IProcessableElementTag?, other: Any?): Boolean {
        return other is IStandaloneElementTag? &&
                self?.elementDefinition == (other as IElementTag?)?.elementDefinition &&
                self?.attributeMap == (other as IProcessableElementTag?)?.attributeMap
    }
}
