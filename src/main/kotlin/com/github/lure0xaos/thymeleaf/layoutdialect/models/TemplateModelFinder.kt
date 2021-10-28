package com.github.lure0xaos.thymeleaf.layoutdialect.models

import com.github.lure0xaos.thymeleaf.layoutdialect.LayoutDialect
import com.github.lure0xaos.thymeleaf.layoutdialect.context.extensions.IContextExtensions.getPrefixForDialect
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.TemplateModel
import org.thymeleaf.standard.expression.FragmentExpression
import org.thymeleaf.standard.expression.IStandardExpression
import java.text.MessageFormat

class TemplateModelFinder(private val context: ITemplateContext) {
    fun findFragment(fragmentExpression: FragmentExpression): TemplateModel {
        val dialectPrefix: String? = getPrefixForDialect(context, LayoutDialect::class.java)
        var templateName = "this"
        val name: IStandardExpression? = fragmentExpression.templateName
        if (name != null) {
            val execute: Any? = name.execute(context)
            if (execute != null) {
                val toString = "$execute"
                if (toString.isNotEmpty()) templateName = toString
            }
        }
        if (templateName == "this") {
            templateName = context.templateData.template
        }
        var fragmentName: String? = null
        val iStandardExpression = fragmentExpression.fragmentSelector
        if (iStandardExpression != null) {
            val execute = iStandardExpression.execute(context)
            if (execute != null) {
                val toString = "$execute"
                if (toString.isNotEmpty()) fragmentName = toString
            }
        }
        return findFragment(templateName, fragmentName, dialectPrefix)
    }

    fun findTemplate(fragmentExpression: FragmentExpression): TemplateModel =
        find(fragmentExpression.templateName.execute(context).toString(), null)

    fun findTemplate(templateName: String): TemplateModel = find(templateName, null)

    private fun find(templateName: String, selector: String?): TemplateModel {
        if (selector != null) {
            return context.configuration.templateManager.parseStandalone(
                context,
                templateName, setOf(selector), context.templateMode, true, true
            )
        }
        return context.configuration.templateManager.parseStandalone(
            context,
            templateName, null, context.templateMode, true, true
        )
    }

    private fun findFragment(templateName: String, fragmentName: String?, dialectPrefix: String?): TemplateModel =
        if (fragmentName != null && dialectPrefix != null)
            find(templateName, MessageFormat.format(FRAGMENT_SELECTOR, dialectPrefix, fragmentName))
        else
            find(templateName, null)

    companion object {
        private const val FRAGMENT_SELECTOR =
            "//[{0}:fragment=''{1}'' or {0}:fragment^=''{1}('' or {0}:fragment^=''{1} ('' or data-{0}-fragment=''{1}'' or data-{0}-fragment^=''{1}('' or data-{0}-fragment^=''{1} ('']"
    }
}
