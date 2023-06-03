package io.github.typesafegithub.workflows.scriptgenerator

import com.squareup.kotlinpoet.CodeBlock

fun String.orExpression(): CodeBlock {
    return GitHubExpression(this).generateExpression()
}

data class GitHubExpression(val input: String) {

    fun generateExpression(): CodeBlock {
        val input = input.trim()
        if (input.startsWith(PREFIX) && input.endsWith(SUFFIX)) {
            val expression = input.removeSuffix(SUFFIX).removePrefix(PREFIX).trim()
            return CodeBlock.of("%M { %S }", Members.expr, expression)
        } else {
            return CodeBlock.of("%S", input)
        }
    }

    companion object {
        const val PREFIX = "\${{"
        const val SUFFIX = "}}"
    }
}
