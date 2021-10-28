package com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions

import org.thymeleaf.model.IModel
import org.thymeleaf.model.ITemplateEvent

internal class EventIterator(private val model: IModel) : Iterator<ITemplateEvent> {
    private var currentIndex = 0
    override fun hasNext(): Boolean = currentIndex < model.size()

    override fun next(): ITemplateEvent {
        val iTemplateEvent = model[currentIndex]
        currentIndex++
        return iTemplateEvent
    }
}
