package com.github.lure0xaos.thymeleaf.expressionprocessor

import org.thymeleaf.context.IExpressionContext
import org.thymeleaf.standard.expression.FragmentExpression
import org.thymeleaf.standard.expression.StandardExpressions

class ExpressionProcessor(private val context: IExpressionContext) {
    fun parseFragmentExpression(expression: String): FragmentExpression =
        StandardExpressions.getExpressionParser(context.configuration)
            .parseExpression(context, expression) as FragmentExpression
}
