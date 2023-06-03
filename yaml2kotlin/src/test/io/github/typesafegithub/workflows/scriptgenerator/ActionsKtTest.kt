package io.github.typesafegithub.workflows.scriptgenerator

import io.github.typesafegithub.workflows.actionsmetadata.model.ActionCoords
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe

class ActionsKtTest : FunSpec(
    {
        val testCases = mapOf(
            "actions/checkout@v1" to ActionCoords("actions", "checkout", "v1"),
            "JamesIves/github-pages-deploy-action@releases/v3" to
                ActionCoords("JamesIves", "github-pages-deploy-action", "releases/v3"),
        )

        test("Create ActionCoords from uses: xxx") {
            testCases.forAll { (input, coords) ->
                ActionCoords(input) shouldBe coords
            }
        }
    },
)
