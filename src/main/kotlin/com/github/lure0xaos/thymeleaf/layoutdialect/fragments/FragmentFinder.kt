package com.github.lure0xaos.thymeleaf.layoutdialect.fragments

import com.github.lure0xaos.thymeleaf.layoutdialect.models.extensions.IModelExtensions.getModel
import org.thymeleaf.model.IModel
import org.thymeleaf.model.IOpenElementTag
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.model.ITemplateEvent

class FragmentFinder(private val dialectPrefix: String) {
    fun findFragments(model: IModel): Map<String, List<IModel>> {
        val fragmentsMap: MutableMap<String, MutableList<IModel>> = mutableMapOf()
        var eventIndex = 0
        while (eventIndex < model.size()) {
            val event: ITemplateEvent = model[eventIndex]
            if (event is IOpenElementTag) {
                val elementTag: IProcessableElementTag = event
                val fragmentName: String? =
                    elementTag.getAttributeValue(dialectPrefix, FragmentProcessor.PROCESSOR_NAME)
                if (!fragmentName.isNullOrEmpty()) {
                    val fragment: IModel = getModel(model, eventIndex)
                    val list: MutableList<IModel> = fragmentsMap.getOrDefault(fragmentName, mutableListOf())
                    list += fragment
                    fragmentsMap[fragmentName] = list
                    eventIndex += fragment.size()
                    continue
                }
            }
            eventIndex++
        }
        return fragmentsMap
    }
}
