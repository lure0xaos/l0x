package com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions

import org.thymeleaf.model.IAttribute

object IAttributeExtensions {
    fun equalsName(self: IAttribute, prefix: String, name: String): Boolean {
        val attributeName = self.attributeCompleteName
        return attributeName == "$prefix:$name" || attributeName == "data-$prefix-$name"
    }
}
