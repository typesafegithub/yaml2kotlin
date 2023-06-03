package io.github.typesafegithub.workflows.scriptmodel

import com.squareup.kotlinpoet.CodeBlock
import io.github.typesafegithub.workflows.domain.Concurrency
import io.github.typesafegithub.workflows.scriptgenerator.EMPTY
import io.github.typesafegithub.workflows.scriptgenerator.joinToCode
import kotlinx.serialization.Serializable

@Serializable
data class YamlWorkflow(
    val name: String,
    val on: YamlWorkflowTriggers,
    val concurrency: Concurrency? = null,
    val jobs: Map<String, YamlJob> = emptyMap(),
    val env: Map<String, String> = emptyMap(),
) {
    override fun toString() = """
       name: $name
       on:
         $on
       jobs:
         ${jobs.entries.joinToString(separator = "\n         ")}
    """.trimIndent()

    fun generateJobs(): CodeBlock =
        jobs.joinToCode(prefix = CodeBlock.EMPTY, postfix = "", newLineAtEnd = false, separator = "\n") { jobId, yamlJob ->
            yamlJob.generateJob(jobId)
        }
}
