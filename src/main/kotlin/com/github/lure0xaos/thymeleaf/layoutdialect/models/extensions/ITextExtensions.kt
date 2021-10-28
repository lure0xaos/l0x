package com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions

import org.thymeleaf.model.IText

internal object ITextExtensions {
    fun equals(self: IText?, other: Any?): Boolean = other is IText? && self?.text == other?.text

    fun isWhitespace(self: IText): Boolean = self.text.trim().isEmpty()
}
