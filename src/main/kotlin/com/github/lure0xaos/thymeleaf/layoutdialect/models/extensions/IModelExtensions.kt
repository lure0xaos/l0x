package com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions

import org.thymeleaf.engine.TemplateModel
import org.thymeleaf.model.*

object IModelExtensions {
    fun asBoolean(self: IModel?): Boolean = self != null && self.size() != 0

    fun childModelIterator(self: IModel): Iterator<IModel> {
        require(isElement(self), self::toString)
        return ChildModelIterator(self)
    }

    fun reversedChildModelIterator(self: IModel): Iterator<IModel> {
        require(isElement(self), self::toString)
        return ReversedChildModelIterator(self)
    }

    fun each(self: IModel, closure: (ITemplateEvent) -> Unit) {
        val iterator = iterator(self)
        while (iterator.hasNext()) closure(iterator.next())
    }

    fun equals(self: IModel?, other: Any?): Boolean =
        if (other !is IModel?) false
        else if (self?.size() != other?.size()) false
        else (0 until (self?.size() ?: 0)).all { ITemplateEventExtensions.equals(self?.get(it), other?.get(it)) }

    fun find(self: IModel, closure: (ITemplateEvent) -> Boolean): ITemplateEvent {
        val iterator = iterator(self)
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (closure(next)) return next
        }
        throw IllegalArgumentException()
    }

    fun findIndexOf(self: IModel, closure: (ITemplateEvent) -> Boolean): Int {
        val iterator = iterator(self)
        var i = 0
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (closure(next)) return i
            i++
        }
        return -1
    }

    fun findIndexOfModel(self: IModel, model: IModel): Int {
        val modelEvent = first(model)
        return findIndexOf(self) { event: ITemplateEvent -> ITemplateEventExtensions.equals(event, modelEvent) }
    }

    fun findModel(self: IModel, closure: (ITemplateEvent) -> Boolean): IModel =
        getModel(self, findIndexOf(self, closure))

    fun first(self: IModel): ITemplateEvent = self[0]

    fun getModel(self: IModel, pos: Int): IModel {
        if (0 > pos || pos >= self.size()) throw ArrayIndexOutOfBoundsException(pos)
        val clone = self.cloneModel()
        var removeBefore: Int = if (self is TemplateModel) pos - 1 else pos
        var removeAfter = clone.size() - (removeBefore + sizeOfModelAt(self, pos))
        while (removeBefore > 0) {
            removeBefore--
            removeFirst(clone)
        }
        while (removeAfter > 0) {
            removeAfter--
            removeLast(clone)
        }
        return clone
    }

    fun insertModelWithWhitespace(self: IModel, pos: Int, model: IModel, modelFactory: IModelFactory) {
        if (0 <= pos && pos <= self.size()) {
            var whitespace = "\t"
            if (pos > 0) {
                for (i in pos - 1 downTo 0) {
                    val event = self[i]
                    if (ITemplateEventExtensions.isWhitespace(event) && (event as IText?)?.text?.isNotEmpty() == true) {
                        whitespace = event.text.replace("[\r\n]".toRegex(), "")
                        break
                    }
                }
            }
            if (pos > 0 && self[pos - 1] is IOpenElementTag && self[pos] is ICloseElementTag) {
                self.insertModel(pos, modelFactory.createModel(modelFactory.createText(System.lineSeparator())))
            }
            self.insertModel(pos, model)
            self.insertModel(
                pos,
                modelFactory.createModel(modelFactory.createText(System.lineSeparator() + whitespace))
            )
        }
    }

    fun insertWithWhitespace(self: IModel, pos: Int, event: ITemplateEvent, modelFactory: IModelFactory) {
        if (0 <= pos && pos <= self.size()) {
            // TODO: Because I can"t check the parent for whitespace hints, I should
            //       make this smarter and find whitespace within the model to copy.
            val whitespace = getModel(self, pos)
            if (isWhitespace(whitespace)) {
                self.insert(pos, event)
                self.insertModel(pos, whitespace)
            } else {
                val newLine = modelFactory.createText("\n")
                if (pos == 0) {
                    self.insert(0, newLine)
                    self.insert(0, event)
                } else if (pos == self.size()) {
                    self.insert(pos, newLine)
                    self.insert(pos, event)
                    self.insert(pos, newLine)
                }
            }
        }
    }

    fun isWhitespace(self: IModel): Boolean = self.size() == 1 && ITemplateEventExtensions.isWhitespace(first(self))

    fun removeAllModels(self: IModel, closure: (ITemplateEvent) -> Boolean) {
        while (true) {
            val modelIndex = findIndexOf(self, closure)
            if (modelIndex == -1) {
                return
            }
            removeModel(self, modelIndex)
        }
    }

    fun removeChildren(self: IModel) {
        if (isElement(self)) {
            while (self.size() > 2) {
                self.remove(1)
            }
        }
    }

    fun replaceModel(self: IModel, pos: Int, model: IModel) {
        if (0 <= pos && pos < self.size()) {
            removeModel(self, pos)
            self.insertModel(pos, model)
        }
    }

    private fun isElement(self: IModel): Boolean = sizeOfModelAt(self, 0) == self.size()

    private fun iterator(self: IModel): Iterator<ITemplateEvent> = EventIterator(self)

    private fun removeFirst(self: IModel) {
        self.remove(0)
    }

    private fun removeLast(self: IModel) {
        self.remove(self.size() - 1)
    }

    private fun removeModel(self: IModel, pos: Int) {
        if (0 <= pos && pos < self.size()) {
            var modelSize = sizeOfModelAt(self, pos)
            while (modelSize > 0) {
                self.remove(pos)
                modelSize--
            }
        }
    }

    private fun sizeOfModelAt(self: IModel, index: Int): Int {
        var eventIndex = index
        var event = self[eventIndex]
        eventIndex++
        if (event is IOpenElementTag) {
            var level = 0
            while (true) {
                event = self[eventIndex]
                eventIndex++
                if (event is IOpenElementTag) {
                    level++
                } else if (event is ICloseElementTag) {
                    if (!event.isUnmatched) {
                        if (level == 0) {
                            break
                        }
                        level--
                    }
                }
            }
            return eventIndex - index
        }
        return 1
    }
}
