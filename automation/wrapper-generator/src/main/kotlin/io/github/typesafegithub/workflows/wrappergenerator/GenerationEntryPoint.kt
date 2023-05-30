package io.github.typesafegithub.workflows.wrappergenerator

import io.github.typesafegithub.workflows.actionsmetadata.model.ActionCoords
import io.github.typesafegithub.workflows.actionsmetadata.model.TypingsSource
import io.github.typesafegithub.workflows.actionsmetadata.model.Version
import io.github.typesafegithub.workflows.actionsmetadata.model.WrapperRequest
import io.github.typesafegithub.workflows.actionsmetadata.wrappersToGenerate
import io.github.typesafegithub.workflows.dsl.expressions.generateEventPayloads
import io.github.typesafegithub.workflows.wrappergenerator.generation.buildActionClassName
import io.github.typesafegithub.workflows.wrappergenerator.generation.generateWrapper
import io.github.typesafegithub.workflows.wrappergenerator.generation.toKotlinPackageName
import io.github.typesafegithub.workflows.wrappergenerator.metadata.deleteActionYamlCacheIfObsolete
import io.github.typesafegithub.workflows.wrappergenerator.metadata.prettyPrint
import io.github.typesafegithub.workflows.wrappergenerator.types.deleteActionTypesYamlCacheIfObsolete
import io.github.typesafegithub.workflows.wrappergenerator.types.provideTypes
import java.nio.file.Path
import java.nio.file.Paths

/***
 * Either run this main() function or run this command: ./gradlew :wrapper-generator:run
 */
fun main() {
    // To ensure there are no leftovers from previous generations.
    Paths.get("library/src/gen").toFile().deleteRecursively()
    generateEventPayloads()
    generateWrappers()
}

private fun generateWrappers() {
    deleteActionYamlCacheIfObsolete()
    deleteActionTypesYamlCacheIfObsolete()

    wrappersToGenerate.forEach { wrapperRequest ->
        println("Generating ${wrapperRequest.actionCoords.prettyPrint}")
        val inputTypings = wrapperRequest.provideTypes()
        val (code, path) = wrapperRequest.actionCoords.generateWrapper(inputTypings)
        with(Paths.get(path).toFile()) {
            parentFile.mkdirs()
            writeText(code)
        }
    }
}

private fun generateListOfWrappersForDocs(listOfWrappersInDocs: Path) {
    listOfWrappersInDocs.toFile().printWriter().use { writer ->
        writer.println(
            """
            This is a complete list of actions for which the library provides typed wrappers, grouped by owners. If your
            action is not on the list, see [Using actions](user-guide/using-actions.md) section.

            Click on a version to see the wrapper's code.

            ## Wrappers

            """.trimIndent(),
        )

        wrappersToGenerate
            .groupBy { it.actionCoords.owner }
            .forEach { (owner, ownedActions) ->
                writer.println("* $owner")
                ownedActions
                    .groupBy { it.actionCoords.name }
                    .forEach { (_, versions) ->
                        val kotlinClasses = versions
                            .sortedBy { Version(it.actionCoords.version) }
                            .joinToString(", ") { it.toMarkdownLinkToKotlinCode() }
                        writer.println("    * ${versions.first().actionCoords.toMarkdownLinkGithub()} - $kotlinClasses")
                    }
            }

        val uniqueActionsCount = wrappersToGenerate.groupBy { "${it.actionCoords.owner}/${it.actionCoords.name}" }.size
        val uniqueActionsProvidingTypingsCount = wrappersToGenerate
            .groupBy { "${it.actionCoords.owner}/${it.actionCoords.name}" }
            .mapValues { (_, versions) -> versions.maxByOrNull { Version(it.actionCoords.version) } }
            .count { (_, wrapperRequest) -> wrapperRequest?.typingsSource == TypingsSource.ActionTypes }

        writer.println(
            """

            ## Statistics

            Number of wrappers available:

            * counting by actions: $uniqueActionsCount
            * counting each version separately: ${wrappersToGenerate.size}

            Actions [providing typings](https://github.com/typesafegithub/github-actions-typing/) (marked with ✅ on the above list): $uniqueActionsProvidingTypingsCount
            """.trimIndent(),
        )
    }
}

private fun WrapperRequest.toMarkdownLinkToKotlinCode(): String {
    val typingsMarker = if (typingsSource == TypingsSource.ActionTypes) " ✅" else ""
    return "${actionCoords.version}$typingsMarker: [`${actionCoords.buildActionClassName()}`](https://github.com/typesafegithub/github-workflows-kt/blob/v[[ version ]]/library/src/gen/kotlin/io/github/typesafegithub/workflows/actions/${actionCoords.owner.toKotlinPackageName()}/${this.actionCoords.buildActionClassName()}.kt)"
}

private fun ActionCoords.toMarkdownLinkGithub() =
    "[$name](https://github.com/$owner/${name.substringBefore('/')}${if ("/" in name) "/tree/$version/${name.substringAfter('/')}" else ""})"
