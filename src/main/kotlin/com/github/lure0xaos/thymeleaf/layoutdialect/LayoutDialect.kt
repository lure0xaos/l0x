package com.github.lure0xaos.thymeleaf.layoutdialect

import com.github.lure0xaos.thymeleaf.layoutdialect.decorators.DecorateProcessor
import com.github.lure0xaos.thymeleaf.layoutdialect.decorators.SortingStrategy
import com.github.lure0xaos.thymeleaf.layoutdialect.decorators.TitlePatternProcessor
import com.github.lure0xaos.thymeleaf.layoutdialect.decorators.strategies.AppendingStrategy
import com.github.lure0xaos.thymeleaf.layoutdialect.fragments.FragmentProcessor
import com.github.lure0xaos.thymeleaf.layoutdialect.includes.InsertProcessor
import com.github.lure0xaos.thymeleaf.layoutdialect.includes.ReplaceProcessor
import org.thymeleaf.dialect.AbstractProcessorDialect
import org.thymeleaf.processor.IProcessor
import org.thymeleaf.standard.processor.StandardXmlNsTagProcessor
import org.thymeleaf.templatemode.TemplateMode

class LayoutDialect(
    private val sortingStrategy: SortingStrategy = AppendingStrategy(),
    private val autoHeadMerging: Boolean = true
) : AbstractProcessorDialect(DIALECT_NAME, DIALECT_PREFIX, DIALECT_PRECEDENCE) {
    override fun getProcessors(dialectPrefix: String): Set<IProcessor> {
        return setOf(
            StandardXmlNsTagProcessor(TemplateMode.HTML, dialectPrefix),
            DecorateProcessor(TemplateMode.HTML, dialectPrefix, sortingStrategy, autoHeadMerging),
            InsertProcessor(TemplateMode.HTML, dialectPrefix),
            ReplaceProcessor(TemplateMode.HTML, dialectPrefix),
            FragmentProcessor(TemplateMode.HTML, dialectPrefix),
            TitlePatternProcessor(TemplateMode.HTML, dialectPrefix),
            StandardXmlNsTagProcessor(TemplateMode.XML, dialectPrefix),
            DecorateProcessor(TemplateMode.XML, dialectPrefix, sortingStrategy, autoHeadMerging),
            InsertProcessor(TemplateMode.XML, dialectPrefix),
            ReplaceProcessor(TemplateMode.XML, dialectPrefix),
            FragmentProcessor(TemplateMode.XML, dialectPrefix)
        )
    }

    companion object {
        const val DIALECT_NAME: String = "Layout"
        const val DIALECT_PRECEDENCE: Int = 10
        const val DIALECT_PREFIX: String = "layout"
    }
}
