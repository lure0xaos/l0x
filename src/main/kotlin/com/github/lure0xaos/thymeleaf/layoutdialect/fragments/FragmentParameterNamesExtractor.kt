package com.github.lure0xaos.thymeleaf.layoutdialect.fragments

import java.util.regex.Pattern

internal object FragmentParameterNamesExtractor {
    private val PATTERN_FRAGMENT = Pattern.compile(".*?\\((.*)\\)")
    private val PATTERN_EXPRESSION = Pattern.compile("([^=]+)=?.*")

    fun extract(fragmentDefinition: String): List<String> {
        val matcher = PATTERN_FRAGMENT.matcher(fragmentDefinition)
        return if (matcher.matches()) matcher.group(1).split(",")
            .map { PATTERN_EXPRESSION.matcher(it).group(1).trim() } else listOf()
    }
}
