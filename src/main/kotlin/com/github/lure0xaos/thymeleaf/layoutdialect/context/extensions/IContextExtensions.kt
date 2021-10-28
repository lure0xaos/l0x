package com.github.lure0xaos.thymeleaf.layoutdialect.context.extensions

import org.thymeleaf.context.IContext
import org.thymeleaf.context.IEngineContext
import org.thymeleaf.context.IExpressionContext
import org.thymeleaf.dialect.IProcessorDialect

object IContextExtensions {
    private const val DIALECT_PREFIX_PREFIX = "DialectPrefix::"

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getAt(self: IContext, name: String): T? = self.getVariable(name) as T?

    fun getPrefixForDialect(self: IContext, dialectClass: Class<out IProcessorDialect>): String? {
        val dialectConfiguration = getOrCreate(self, DIALECT_PREFIX_PREFIX + dialectClass.name) {
            if (self !is IExpressionContext) return@getOrCreate null
            val configuration = self.configuration ?: return@getOrCreate null
            val configurations = configuration.dialectConfigurations ?: return@getOrCreate null
            for (dialectConfig in configurations) {
                if (dialectClass.isInstance(dialectConfig.dialect)) return@getOrCreate dialectConfig
            }
            null
        } ?: return null
        if (dialectConfiguration.isPrefixSpecified) return dialectConfiguration.prefix
        val dialect = dialectConfiguration.dialect
        if (dialect is IProcessorDialect) return dialect.prefix
        return null
    }

    fun putAt(self: IContext, name: String, value: Any?) {
        if (self is IEngineContext) self.setVariable(name, value)
    }

    private fun <T : Any> getOrCreate(self: IContext, name: String, creator: () -> T?): T? {
        val value: T? = getAt(self, name)
        if (value != null) {
            return value
        }
        val newValue: T? = creator()
        putAt(self, name, newValue)
        return newValue
    }
}
