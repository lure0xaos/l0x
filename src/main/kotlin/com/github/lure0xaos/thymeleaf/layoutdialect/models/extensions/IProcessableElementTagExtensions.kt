package com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions

import org.thymeleaf.model.IElementTag
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.model.IStandaloneElementTag

object IProcessableElementTagExtensions {
    fun equals(self: IProcessableElementTag?, other: Any?): Boolean {
        return if (self is IStandaloneElementTag?) IStandaloneElementTagExtensions.equals(self, other)
        else other is IProcessableElementTag? &&
                self?.elementDefinition == (other as IElementTag?)?.elementDefinition &&
                self?.attributeMap == other?.attributeMap
    }

    fun equalsIgnoreXmlns(self: IProcessableElementTag, other: IProcessableElementTag): Boolean {
        if (self.elementDefinition != other.elementDefinition) return false
        val difference: MutableMap<String, String> = HashMap(self.attributeMap)
        for (key in other.attributeMap.keys) difference.remove(key)
        return if (difference.isEmpty()) true else difference.keys.all { it.startsWith("xmlns:") }
    }
}
