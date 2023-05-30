package test

import io.github.typesafegithub.workflows.scriptgenerator.decodeYamlWorkflow
import io.github.typesafegithub.workflows.scriptgenerator.toFileSpec
import io.github.typesafegithub.workflows.scriptgenerator.workflowVariableName
import io.github.typesafegithub.workflows.scriptmodel.YamlWorkflow
import io.github.typesafegithub.workflows.yaml.writeToFile
import io.kotest.assertions.fail
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import java.io.File

@Suppress("UNUSED_VARIABLE")
class RunSamples : FunSpec(
    {
        val files = sampleFiles

        test("Folder /samples must exist") {
            withClue(samples.canonicalPath) {
                files.shouldNotBeEmpty()
            }
        }
        val sampleFiles = files.filter {
            it.extension in Conventions.yamlSuffixes
        }

        test("Kotlin files - fix package actual => expected") {
            val expected = files.filter { it.nameWithoutExtension.endsWith("Expected") }
            expected.forEach { file ->
                val newText = file.readText().replace(Conventions.actualPackage, Conventions.expectedPackage)
                file.writeText(newText)
            }
        }
        test("Remove stale files") {
            val inputFiles = sampleFiles
                .map {
                    it.nameWithoutExtension
                }
                .toSet()
            val kotlinFiles = files.filter {
                it.extension == "kt" && it.nameWithoutExtension !in inputFiles && it != Conventions.workflowsFile
            }.map { it.name }
            kotlinFiles shouldBe emptyList()
        }

        test("Generate workflow files") {
            val workflowVariableNames = sampleFiles.map {
                workflowVariableName(it.nameWithoutExtension)
            }

            Conventions.workflowsFile.writeText(workflowsList(workflowVariableNames))
        }

        context("Generate Kotlin files") {

            sampleFiles.forEach { sample ->
                test("/samples/${sample.nameWithoutExtension}.kt from yaml") {
                    val regex = "[A-Z][A-Za-z0-9]+\\.(yaml|yml)".toRegex()
                    sample.name.shouldMatch(regex)

                    val input = SampleInput(sample)
                    val yaml = input.yamlFile.readText()
                    val workflow: YamlWorkflow = decodeYamlWorkflow(yaml)
                    val newContent = workflow
                        .toFileSpec(input.filename)
                        .toString()
                        .withoutPackages()

                    input.kotlinFile.writeText(
                        "package expected\n$newContent",
                    )

                    if (isGitHubActions()) {
                        newContent shouldBe input.expected
                    } else if (newContent != input.expected) {
                        fail("Double-check changes to samples/${input.kotlinFile.name}")
                    }
                }
            }
        }

        test("Execute Kotlin Scripts") {
            val gitRootDir = tempdir().also {
                it.resolve(".git").mkdirs()
            }.toPath()
            expected.allWorkflows.forEach {
                it.copy(sourceFile = gitRootDir.resolve(it.sourceFile))
                    .writeToFile(addConsistencyCheck = false)
            }
        }
    },
)

object Conventions {
    val yamlSuffixes = setOf("yml", "yaml")
    const val actual = "actual"
    const val expected = "expected"
    val actualPackage = "package $actual"
    val expectedPackage = "package $expected"
    val workflowsFile = samples.resolve("_ALL_.kt")
}

data class SampleInput(val yamlFile: File) {
    init {
        require(yamlFile.canRead()) { "Invalid file ${yamlFile.canonicalPath}" }
    }

    val filename = yamlFile.nameWithoutExtension
    fun file(path: String) = samples.resolve(path)

    val kotlinFile = samples.resolve("$filename.kt")

    val expected = if (kotlinFile.canRead()) {
        kotlinFile.readText().withoutPackages()
    } else {
        ""
    }
}

private fun String.withoutPackages() =
    removePrefix("package actual")
        .removePrefix("package expected")
        .removePrefix("\n")
        .removeWindowsEndings()
        .removeSuffix("\n")


fun workflowsList(kotlinVariables: List<String>) = """
package expected

import io.github.typesafegithub.workflows.domain.Workflow

val allWorkflows: List<Workflow> = listOf(
${kotlinVariables.joinToString(",\n   ", prefix = "     ")}
)
"""
