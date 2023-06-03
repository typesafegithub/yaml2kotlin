package io.github.typesafegithub.workflows.scriptgenerator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.typesafegithub.workflows.actionsmetadata.model.ActionCoords
import io.github.typesafegithub.workflows.actionsmetadata.model.StringTyping
import io.github.typesafegithub.workflows.actionsmetadata.model.Typing
import io.github.typesafegithub.workflows.domain.actions.CustomAction
import io.github.typesafegithub.workflows.scriptmodel.YamlStep
import io.github.typesafegithub.workflows.wrappergenerator.generation.buildActionClassName
import io.github.typesafegithub.workflows.wrappergenerator.generation.toCamelCase
import io.github.typesafegithub.workflows.wrappergenerator.generation.toKotlinPackageName



fun ActionCoords(yaml: String): ActionCoords {
    val (repo, version) = yaml.split("@")
    val (owner, name) = repo.split("/")
    return ActionCoords(owner, name, version)
}

fun ActionCoords.classname() =
    ClassName("$PACKAGE.actions.${owner.toKotlinPackageName()}", buildActionClassName())

fun ActionCoords.toMap(): Map<String, String> =
    mapOf(
        "actionOwner" to owner,
        "actionName" to name,
        "actionVersion" to version,
    )
