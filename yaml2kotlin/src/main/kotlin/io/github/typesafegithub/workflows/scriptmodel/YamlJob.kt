package io.github.typesafegithub.workflows.scriptmodel

import com.squareup.kotlinpoet.CodeBlock
import io.github.typesafegithub.workflows.domain.Concurrency
import io.github.typesafegithub.workflows.scriptgenerator.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YamlJob(
    val name: String? = null,
    @SerialName("runs-on")
    val runsOn: String,
    val steps: List<YamlStep>,
    val needs: List<String> = emptyList(),
    val env: LinkedHashMap<String, String> = linkedMapOf(),
    val condition: String? = null,
    val strategyMatrix: Map<String, List<String>>? = null,
    val concurrency: Concurrency? = null,
) {
    fun generateJob(jobId: String) = CodeBlock { builder ->
        builder.add("job(\n")
            .indent()
            .add("id = %S,\n", jobId)
        if (name != null) {
            builder.add("name = %S,\n", name)
        }
        builder.add(runnerTypeBlockOf(runsOn))
        builder.add(concurrencyOf(concurrency))
        builder.add(
            env.joinToCode(
                ifEmpty = CodeBlock.EMPTY,
                prefix = CodeBlock.of("env = %M(", Members.linkedMapOf),
                postfix = "),\n",
                transform = { key, value -> CodeBlock.of("%S to %S", key, value) },
            ),
        )
        builder.add(customArguments())
        builder.unindent()
        builder.add(") {\n")
        builder.indent()
        steps.forEach { step ->
            builder.add(step.generateStep())
        }
        builder.unindent()
            .add("}\n\n")
    }


    fun customArguments(): CodeBlock {
        val map = listOfNotNull(
            ("needs" to needs).takeIf { needs.isNotEmpty() },
        ).toMap()
        return map.joinToCode(
            ifEmpty = CodeBlock.EMPTY,
            prefix = CodeBlock.of("_customArguments = %M(\n", Members.mapOf),
            separator = "",
            postfix = ")",
            transform = { key, list ->
                list.joinToCode(
                    prefix = CodeBlock.of("%S to %M(", key, Members.listOf),
                    separator = ", ",
                    postfix = "),\n",
                    newLineAtEnd = false,
                    transform = { CodeBlock.of("%S", it) },
                )
            },
        )
    }
}
