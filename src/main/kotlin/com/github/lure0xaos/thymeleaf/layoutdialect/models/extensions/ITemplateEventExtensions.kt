package com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions

import org.thymeleaf.model.*

object ITemplateEventExtensions {
    fun equals(self: ITemplateEvent?, other: ITemplateEvent?): Boolean = when (self) {
        is IText -> ITextExtensions.equals(self, other)
        is ICloseElementTag -> ICloseElementTagExtensions.equals((self as ICloseElementTag?)!!, other)
        is IProcessableElementTag -> IProcessableElementTagExtensions
            .equals((self as IProcessableElementTag?)!!, other)

        else -> self == other
    }

    fun isClosingElementOf(self: ITemplateEvent, tagName: String): Boolean =
        isClosingElement(self) && (self as IElementTag?)?.elementCompleteName == tagName

    fun isOpeningElementOf(self: ITemplateEvent, tagName: String): Boolean =
        isOpeningElement(self) && (self as IElementTag?)?.elementCompleteName == tagName

    fun isWhitespace(self: ITemplateEvent): Boolean = self is IText && ITextExtensions.isWhitespace(self)

    private fun isClosingElement(self: ITemplateEvent): Boolean =
        self is ICloseElementTag || self is IStandaloneElementTag

    private fun isOpeningElement(self: ITemplateEvent): Boolean =
        self is IOpenElementTag || self is IStandaloneElementTag
}
