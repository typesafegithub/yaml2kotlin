package io.github.typesafegithub.workflows.scriptgenerator

import com.squareup.kotlinpoet.CodeBlock
import io.github.typesafegithub.workflows.actionsmetadata.model.*
import io.github.typesafegithub.workflows.wrappergenerator.generation.buildActionClassName
import io.github.typesafegithub.workflows.wrappergenerator.generation.toPascalCase

fun CodeBlock.Builder.addConditionMaybe(condition: String?) {
    if (condition != null) {
        add("condition = ")
            .add(condition.orExpression())
            .add(",\n")
    }
}


fun valueWithTyping(value: String, typing: Typing, coords: ActionCoords): CodeBlock {
    val classname = coords.buildActionClassName()
    return when (typing) {
        is EnumTyping -> {
            val itemsNames = typing.itemsNames ?: typing.items.map { it.toPascalCase() }
            val itemsNameMap = typing.items.zip(itemsNames).toMap()
            val enumName = itemsNameMap[value]?.toPascalCase()
            CodeBlock.of("%L", "$classname.${typing.typeName}.$enumName")
        }

        is BooleanTyping ->
            CodeBlock.of("%L", value)

        is IntegerTyping ->
            value.toIntOrNull()
                ?.let { CodeBlock.of("%L", value) }
                ?: CodeBlock.of("%S", value)

        is IntegerWithSpecialValueTyping -> when (typing.specialValues.containsValue(value.toInt())) {
            true -> {
                val specialValue = typing.specialValues.keys.first { typing.specialValues[it] == value.toInt() }
                CodeBlock.of("%L", "$classname.${typing.typeName}.${specialValue.toPascalCase()}")
            }

            false -> CodeBlock.of("%L", "$classname.${typing.typeName}.Value($value)")
        }

        is ListOfTypings -> {
            val delimeter = if (typing.delimiter == "\\n") "\n" else typing.delimiter
            val values = value.trim().split(delimeter)

            values.joinToCode(
                prefix = CodeBlock.of("listOf("),
                postfix = ")",
            ) { elem ->
                valueWithTyping(elem, typing.typing, coords)
            }
        }

        else ->
            CodeBlock.of("%S", value)
    }
}
