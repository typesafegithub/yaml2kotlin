package test

import io.github.typesafegithub.workflows.scriptgenerator.decodeYamlWorkflow
import io.github.typesafegithub.workflows.scriptgenerator.toFileSpec
import io.github.typesafegithub.workflows.scriptgenerator.workFlowProperty
import io.github.typesafegithub.workflows.scriptgenerator.workflowVariableName
import io.github.typesafegithub.workflows.scriptmodel.YamlWorkflow
import io.github.typesafegithub.workflows.yaml.writeToFile
import io.kotest.assertions.fail
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldBeEmpty
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

        test("Kotlin files must end in ${Conventions.kotlinSuffixes}") {
            val kotlinFiles = files.filter { it.extension == "kt" }
            val invalid = kotlinFiles.filter { file ->
                Conventions.kotlinSuffixes.none { file.name.endsWith(it) }
            }
            invalid.shouldBeEmpty()
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
                var name = it.name
                Conventions.kotlinSuffixes.forEach { name = name.removeSuffix(it) }
                it.extension == "kt" && name !in inputFiles && name.isNotBlank()
            }.map { it.name }
            kotlinFiles shouldBe emptyList()
        }

        test("Generate workflow files") {
            val workflowVariableNames = sampleFiles.map {
                workflowVariableName(it.nameWithoutExtension + "generated-${it.extension}")
            }

            Conventions.workflowsFile.writeText(workflowsList(workflowVariableNames))
        }

        context("Generate Kotlin files") {
            val UPDATE_FILES = false // only use if you plan make massive changes in the files

            sampleFiles.forEach { sample ->
                test("/samples/${sample.name}") {
                    val regex = "[A-Z][A-Za-z0-9]+\\.(yaml|yml)".toRegex()
                    sample.name.shouldMatch(regex)

                    val input = SampleInput(sample)
                    withClue(input) {
                        val yaml = input.yamlFile.readText()
                        val workflow: YamlWorkflow = decodeYamlWorkflow(yaml)
                        val newContent = workflow.toFileSpec(input.generatedYaml).toString()
                            .withoutPackages()

                        input.actualFile.writeText(
                                "package actual\n$newContent"
                        )

                        if (isGitHubActions()) {
                            newContent shouldBe input.expected
                        } else if (UPDATE_FILES) {
                            input.expectedFile.writeText("package expected\n$newContent",)
                            input.actualFile.delete()
                        } else if (newContent == input.expected) {
                            input.actualFile.delete()
                        } else {
                            fail("${input.expectedFile.name} != ${input.actualFile.name} in ${input.actualFile.parentFile.canonicalPath}")
                        }
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
    val kotlinSuffixes = setOf("Expected.kt", "Actual.kt", "_ALL_.kt")
    val actualPackage = "package $actual"
    val expectedPackage = "package $expected"
    val workflowsFile = samples.resolve("_ALL_.kt")
}

data class SampleInput(val yamlFile: File) {
    init {
        require(yamlFile.canRead()) { "Invalid file ${yamlFile.canonicalPath}" }
    }

    val filename = yamlFile.nameWithoutExtension
    val generatedYaml = yamlFile.nameWithoutExtension + "Generated." + yamlFile.extension
    fun file(path: String) = samples.resolve(path)

    val expectedFile = samples.resolve("${filename}Expected.kt")
    val actualFile = samples.resolve("${filename}Actual.kt")

    val expected = if (expectedFile.canRead()) {
        expectedFile.readText()
            .withoutPackages()
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
