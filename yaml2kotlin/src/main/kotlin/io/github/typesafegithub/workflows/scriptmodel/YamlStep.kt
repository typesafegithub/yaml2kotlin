package io.github.typesafegithub.workflows.scriptmodel

import com.squareup.kotlinpoet.CodeBlock
import io.github.typesafegithub.workflows.actionsmetadata.model.ActionCoords
import io.github.typesafegithub.workflows.actionsmetadata.model.StringTyping
import io.github.typesafegithub.workflows.actionsmetadata.model.Typing
import io.github.typesafegithub.workflows.actionsmetadata.model.WrapperRequest
import io.github.typesafegithub.workflows.actionsmetadata.wrappersToGenerate
import io.github.typesafegithub.workflows.domain.actions.CustomAction
import io.github.typesafegithub.workflows.scriptgenerator.ActionCoords
import io.github.typesafegithub.workflows.scriptgenerator.CodeBlock
import io.github.typesafegithub.workflows.scriptgenerator.EMPTY
import io.github.typesafegithub.workflows.scriptgenerator.Members
import io.github.typesafegithub.workflows.scriptgenerator.addConditionMaybe
import io.github.typesafegithub.workflows.scriptgenerator.classname
import io.github.typesafegithub.workflows.scriptgenerator.joinToCode
import io.github.typesafegithub.workflows.scriptgenerator.orExpression
import io.github.typesafegithub.workflows.scriptgenerator.toMap
import io.github.typesafegithub.workflows.scriptgenerator.valueWithTyping
import io.github.typesafegithub.workflows.wrappergenerator.generation.buildActionClassName
import io.github.typesafegithub.workflows.wrappergenerator.generation.toCamelCase
import io.github.typesafegithub.workflows.wrappergenerator.types.provideTypes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YamlStep(
    val id: String? = null,
    val name: String? = null,
    val run: String? = null,
    val uses: String? = null,
    val with: LinkedHashMap<String, String?> = linkedMapOf(),
    val env: LinkedHashMap<String, String?> = linkedMapOf(),
    @SerialName("if")
    val condition: String? = null,
) {
    fun generateStep(): CodeBlock =
        if (uses != null) {
            generateUsesAction(uses)
        } else {
            generateRun()
        }

    fun generateRun(): CodeBlock = CodeBlock { builder ->
        builder
            .add(("run(\n"))
            .indent()
            .add("name = %S,\n", name ?: run)
            .add("command = %S,\n", run)
            .add(generateEnvironnement())
        builder.addConditionMaybe(condition)
        builder.unindent().add(")\n")
    }

    fun generateEnvironnement(): CodeBlock = env.joinToCode(
        prefix = CodeBlock.of("%L = linkedMapOf(\n", "env"),
        postfix = "),",
        ifEmpty = CodeBlock.EMPTY,
    ) { key, value ->
        value?.let {
            CodeBlock { builder ->
                builder.add("%S to ", key).add(value.orExpression())
            }
        }
    }

    fun YamlStep.generateUsesAction(uses: String): CodeBlock {
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
        return generateUsesAction(wrapper?.actionCoords ?: coords, inputTypings, _customVersion)
    }

    fun generateUsesAction(
        coords: ActionCoords,
        inputTypings: Map<String, Typing>?,
        _customVersion: String?,
    ) = CodeBlock { builder ->

        builder.add("uses(\n")
        builder.indent()
        builder.add("name = %S,\n", name ?: coords.buildActionClassName())

        if (inputTypings == null) {
            builder.add(generateCustomAction(coords))
        } else if (with.isEmpty() && _customVersion == null) {
            builder.add("action = %T(),\n", coords.classname())
        } else {
            builder.add(generateActionWithWrapper(coords, inputTypings, _customVersion))
        }

        builder.add(addEnvironnement())
        builder.addConditionMaybe(condition)
        builder.unindent()
        builder.add(")\n")
    }

    fun addEnvironnement() = env.joinToCode(
        prefix = CodeBlock.of("%L = linkedMapOf(\n", "env"),
        postfix = "),",
        ifEmpty = CodeBlock.EMPTY,
    ) { key, value ->
        value?.let {
            CodeBlock { builder ->
                builder.add(CodeBlock.of("%S to ", key))
                    .add(value.orExpression())
            }
        }
    }

    fun generateActionWithWrapper(
        coords: ActionCoords,
        inputTypings: Map<String, Typing>?,
        _customVersion: String?,
    ): CodeBlock {
        val kclass = Class.forName(coords.classname().reflectionName()).kotlin
        val kclassProperties = kclass.members.map { it.name }.toSet()

        val existingActionProperties = with
            .filterKeys { key -> key.toCamelCase() in kclassProperties }
            .joinToCode(
                prefix = CodeBlock.of("action = %T(", coords.classname()),
                postfix = "",
                newLineAtEnd = false,
            ) { key, value ->
                existingActionProperty(coords, inputTypings, value, key)
            }
        val customActionProperties = with
            .filterKeys { key -> key.toCamelCase() !in kclassProperties }
            .joinToCode(
                ifEmpty = CodeBlock.EMPTY,
                prefix = CodeBlock.of("_customInputs = mapOf(\n"),
                newLineAtEnd = false,
                postfix = "),\n",
            ) { key, value ->
                CodeBlock.of("%S to %S", key, value)
            }
        return CodeBlock { builder ->
            builder.add(existingActionProperties)
            builder.indent()
            builder.add(customActionProperties)
            if (_customVersion != null) {
                builder.add("_customVersion = %S,\n", _customVersion)
            }
            builder.unindent()
            builder.add("),\n")
        }
    }

    fun existingActionProperty(
        coords: ActionCoords,
        inputTypings: Map<String, Typing>?,
        value: String?,
        key: String,
    ) = CodeBlock { builder ->
        value ?: return@CodeBlock
        val typing = inputTypings?.get(key) ?: StringTyping
        builder
            .add("%N = ", key.toCamelCase())
            .add(valueWithTyping(value, typing, coords))
            .add(",\n")
    }

    fun generateCustomAction(
        coords: ActionCoords,
    ): CodeBlock {
        val coordsBlock = coords.toMap().joinToCode(
            prefix = CodeBlock.of("action = %T(", CustomAction::class),
            postfix = "",
            newLineAtEnd = false,
        ) { key, value ->
            CodeBlock.of("%L = %S", key.toCamelCase(), value)
        }

        val inputsBlock = with.joinToCode(
            prefix = CodeBlock.of("inputs = %M(\n", Members.mapOf),
            ifEmpty = CodeBlock.of("inputs = emptyMap()"),
        ) { key, value ->
            CodeBlock.of("%S to %S", key, value)
        }

        return CodeBlock { builder ->
            builder.add(coordsBlock)
            builder.indent()
            builder.add(inputsBlock)
            builder.unindent()
            builder.add("),\n")
        }
    }
}
