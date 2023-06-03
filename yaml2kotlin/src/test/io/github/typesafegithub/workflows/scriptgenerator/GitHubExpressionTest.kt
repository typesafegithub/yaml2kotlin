package io.github.typesafegithub.workflows.scriptgenerator

import io.github.typesafegithub.workflows.scriptgenerator.GitHubExpression.Companion.PREFIX
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe

class GitHubExpressionTest : FunSpec(
    {
        val expr = Members.expr.toString()
        val testCases: Map<String, String> = mapOf(
            "always()" to "\"always()\"",
            "$PREFIX  always() }}" to "$expr { \"always()\" }",
        )

        test("Convert GitHub Expression from String to CodeBlock") {
            testCases.forAll { (input, block) ->
                input.orExpression().toString() shouldBe block
            }
        }
    },
)
