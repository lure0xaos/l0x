package com.github.lure0xaos.thymeleaf.layoutdialect.fragments.extensions

import com.github.lure0xaos.thymeleaf.layoutdialect.context.extensions.IContextExtensions.getAt
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.IModel
import org.thymeleaf.processor.element.IElementModelStructureHandler

object FragmentExtensions {
    private const val FRAGMENT_COLLECTION_KEY = "LayoutDialect::FragmentCollection"

    fun getFragmentCollection(self: ITemplateContext, fromDecorator: Boolean): Map<String, List<IModel>> {
        // If the template stack contains only 1 template and we've been called from
        // the decorator, then always return a new fragment collection.  This seems
        // to be one way to know if Thymeleaf is processing a new file and as such
        // should have a fresh collection to work with, otherwise we may be using an
        // older collection from an already-used context.
        // See: https://github.com/ultraq/thymeleaf-layout-dialect/issues/189
        return if (self.templateStack.size == 1 && fromDecorator) mapOf()
        else getAt(self, FRAGMENT_COLLECTION_KEY) ?: return mapOf()
    }

    fun setLocalFragmentCollection(
        self: IElementModelStructureHandler, context: ITemplateContext,
        fragments0: Map<String, List<IModel>>, fromDecorator: Boolean,
    ) {
        val fragments: MutableMap<String, MutableList<IModel>> = mutableMapOf()
        fragments0.forEach { (key: String, list: List<IModel>) -> fragments[key] = list.toMutableList() }
        val fragmentCollection: Map<String, List<IModel>> = getFragmentCollection(context, fromDecorator)
        for ((fragmentName, fragmentList) in fragmentCollection) {
            if (fragments.containsKey(fragmentName)) {
                val subFragments = fragments[fragmentName] ?: return
                subFragments.addAll(fragmentList)
                if (!fromDecorator) subFragments.reverse()
            } else {
                fragments[fragmentName] = fragmentList.toMutableList()
            }
        }
        self.setLocalVariable(FRAGMENT_COLLECTION_KEY, fragments)
    }
}
