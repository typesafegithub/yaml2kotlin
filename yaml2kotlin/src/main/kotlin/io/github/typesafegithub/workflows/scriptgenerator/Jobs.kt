package io.github.typesafegithub.workflows.scriptgenerator

import com.squareup.kotlinpoet.CodeBlock
import io.github.typesafegithub.workflows.actionsmetadata.model.WrapperRequest
import io.github.typesafegithub.workflows.actionsmetadata.wrappersToGenerate
import io.github.typesafegithub.workflows.scriptmodel.YamlJob
import io.github.typesafegithub.workflows.scriptmodel.YamlStep
import io.github.typesafegithub.workflows.scriptmodel.YamlWorkflow
import io.github.typesafegithub.workflows.scriptmodel.runnerTypeBlockOf
import io.github.typesafegithub.workflows.wrappergenerator.generation.buildActionClassName
import io.github.typesafegithub.workflows.wrappergenerator.types.provideTypes

fun YamlWorkflow.generateJobs() =
    jobs.joinToCode(prefix = CodeBlock.EMPTY, postfix = "", newLineAtEnd = false, separator = "\n") { jobId, yamlJob ->
        yamlJob.generateJob(jobId)
    }

fun YamlJob.generateJob(jobId: String) = CodeBlock { builder ->
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
        step.generate(builder)
    }
    builder.unindent()
        .add("}\n\n")
}

private fun YamlStep.generate(
    builder: CodeBlock.Builder,
) {
    if (uses != null) {
        val coords = ActionCoords(uses)
        val (owner, name) = coords
        val availableWrappers = wrappersToGenerate.filter {
            val (theOwner, theName) = it.actionCoords
            owner == theOwner && name == theName
        }
        val wrapper: WrapperRequest? = availableWrappers.firstOrNull {
            it.actionCoords.buildActionClassName() == coords.buildActionClassName()
        } ?: availableWrappers.maxByOrNull { it.actionCoords.version }
        val _customVersion = coords.version.takeIf { it != wrapper?.actionCoords?.version }
        val inputTypings = wrapper?.provideTypes()
        builder.add(generateAction(wrapper?.actionCoords ?: coords, inputTypings, _customVersion))
    } else {
        builder.add(generateCommand())
    }
}

private fun YamlJob.customArguments(): CodeBlock {
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

fun YamlStep.generateCommand() = CodeBlock { builder ->
    builder
        .add(("run(\n"))
        .indent()
        .add("name = %S,\n", name ?: run)
        .add("command = %S,\n", run)
        .add(
            env.joinToCode(
                prefix = CodeBlock.of("%L = linkedMapOf(\n", "env"),
                postfix = "),",
                ifEmpty = CodeBlock.EMPTY,
            ) { key, value ->
                value?.let {
                    CodeBlock { builder ->
                        builder.add("%S to ", key).add(value.orExpression())
                    }
                }
            },
        )
    if (condition != null) {
        builder.add("condition = ")
            .add(condition.orExpression())
            .add(",\n")
    }
    builder.unindent().add(")\n")
}
