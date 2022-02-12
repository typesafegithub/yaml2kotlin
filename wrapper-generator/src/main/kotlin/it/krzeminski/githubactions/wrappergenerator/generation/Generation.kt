package it.krzeminski.githubactions.wrappergenerator.generation

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import it.krzeminski.githubactions.wrappergenerator.domain.ActionCoords
import it.krzeminski.githubactions.wrappergenerator.domain.typings.EnumTyping
import it.krzeminski.githubactions.wrappergenerator.domain.typings.StringTyping
import it.krzeminski.githubactions.wrappergenerator.domain.typings.Typing
import it.krzeminski.githubactions.wrappergenerator.metadata.Input
import it.krzeminski.githubactions.wrappergenerator.metadata.Metadata
import it.krzeminski.githubactions.wrappergenerator.metadata.fetchMetadata

data class Wrapper(
    val kotlinCode: String,
    val filePath: String,
)

fun ActionCoords.generateWrapper(
    inputTypings: Map<String, Typing> = emptyMap(),
    fetchMetadataImpl: ActionCoords.() -> Metadata = { fetchMetadata() },
): Wrapper {
    val metadata = fetchMetadataImpl()
    val actionWrapperSourceCode = generateActionWrapperSourceCode(metadata, this, inputTypings)
    return Wrapper(
        kotlinCode = actionWrapperSourceCode,
        filePath = "library/src/gen/kotlin/it/krzeminski/githubactions/actions/${owner.toKotlinPackageName()}/${this.buildActionClassName()}.kt",
    )
}

private fun generateActionWrapperSourceCode(metadata: Metadata, coords: ActionCoords, inputTypings: Map<String, Typing>): String {
    val fileSpec = FileSpec.builder("it.krzeminski.githubactions.actions.${coords.owner.toKotlinPackageName()}", coords.buildActionClassName())
        .addComment(
            """
            This file was generated using 'wrapper-generator' module. Don't change it by hand, your changes will
            be overwritten with the next wrapper code regeneration. Instead, consider introducing changes to the
            generator itself.
            """.trimIndent()
        )
        .addType(generateActionClass(metadata, coords, inputTypings))
        .indent("    ")
        .build()
    return buildString {
        fileSpec.writeTo(this)
    }
}

private fun generateActionClass(metadata: Metadata, coords: ActionCoords, inputTypings: Map<String, Typing>): TypeSpec {
    val actionClassName = coords.buildActionClassName()
    return TypeSpec.classBuilder(actionClassName)
        .addKdoc(actionKdoc(metadata, coords))
        .inheritsFromAction(coords)
        .primaryConstructor(metadata.primaryConstructor(inputTypings, coords))
        .properties(metadata, coords, inputTypings)
        .addFunction(metadata.buildToYamlArgumentsFunction(inputTypings))
        .addCustomTypes(inputTypings.values.toSet(), coords)
        .build()
}

private fun TypeSpec.Builder.addCustomTypes(typings: Set<Typing>, coords: ActionCoords): TypeSpec.Builder {
    typings
        .filterIsInstance<EnumTyping>()
        .forEach { addType(it.buildSealedClass(coords)) }
    return this
}

private fun EnumTyping.buildSealedClass(coords: ActionCoords): TypeSpec {
    val actionPackageName = coords.owner.toKotlinPackageName()
    val actionClassName = coords.buildActionClassName()
    val sealedClassName = this.getClassName(actionPackageName, actionClassName)
    return TypeSpec.classBuilder(this.typeName)
        .addModifiers(KModifier.SEALED)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(ParameterSpec.builder("stringValue", String::class).build())
                .build()
        )
        .addProperty(PropertySpec.builder("stringValue", String::class).initializer("stringValue").build())
        .addTypes(
            this.items.map {
                TypeSpec.objectBuilder(it.toPascalCase())
                    .superclass(sealedClassName)
                    .addSuperclassConstructorParameter("%S", it)
                    .build()
            }
        )
        .addType(
            TypeSpec.classBuilder("Custom")
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder("customStringValue", String::class).build())
                        .build()
                )
                .superclass(sealedClassName)
                .addSuperclassConstructorParameter("customStringValue")
                .build()
        )
        .build()
}

private fun TypeSpec.Builder.properties(metadata: Metadata, coords: ActionCoords, inputTypings: Map<String, Typing>): TypeSpec.Builder {
    metadata.inputs.forEach { (key, input) ->
        addProperty(
            PropertySpec.builder(key.toCamelCase(), inputTypings.getInputType(key, input, coords))
                .initializer(key.toCamelCase())
                .build()
        )
    }
    return this
}

private fun Metadata.buildToYamlArgumentsFunction(inputTypings: Map<String, Typing>) =
    FunSpec.builder("toYamlArguments")
        .addModifiers(KModifier.OVERRIDE)
        .addAnnotation(
            AnnotationSpec.builder(Suppress::class)
                .addMember("\"SpreadOperator\"")
                .build()
        )
        .addCode(
            CodeBlock.Builder().apply {
                add("return linkedMapOf(\n")
                indent()
                add("*listOfNotNull(\n")
                indent()
                inputs.forEach { (key, value) ->
                    val asStringCode = inputTypings.getInputTyping(key).asString()
                    if (!value.shouldBeNonNullInWrapper()) {
                        add("%L?.let { %S to it$asStringCode },\n", key.toCamelCase(), key)
                    } else {
                        add("%S to %L$asStringCode,\n", key, key.toCamelCase())
                    }
                }
                unindent()
                add(").toTypedArray()\n")
                unindent()
                add(")")
            }.build()
        )
        .build()

private fun TypeSpec.Builder.inheritsFromAction(coords: ActionCoords): TypeSpec.Builder = this
    .superclass(ClassName("it.krzeminski.githubactions.actions", "Action"))
    .addSuperclassConstructorParameter("%S", coords.owner)
    .addSuperclassConstructorParameter("%S", coords.name)
    .addSuperclassConstructorParameter("%S", coords.version)

private fun Metadata.primaryConstructor(inputTypings: Map<String, Typing>, coords: ActionCoords): FunSpec {
    return FunSpec.constructorBuilder()
        .addParameters(
            inputs.map { (key, input) ->
                ParameterSpec.builder(key.toCamelCase(), inputTypings.getInputType(key, input, coords))
                    .defaultValueIfNullable(input)
                    .addKdoc(input.description)
                    .build()
            }
        )
        .build()
}

private fun ParameterSpec.Builder.defaultValueIfNullable(input: Input): ParameterSpec.Builder {
    if (!input.shouldBeNonNullInWrapper()) {
        defaultValue("null")
    }
    return this
}

private fun actionKdoc(metadata: Metadata, coords: ActionCoords) =
    """
        Action: ${metadata.name}

        ${metadata.description}

        https://github.com/${coords.owner}/${coords.name}
    """.trimIndent()

private fun Map<String, Typing>.getInputTyping(key: String) =
    this[key] ?: StringTyping

private fun Map<String, Typing>.getInputType(key: String, input: Input, coords: ActionCoords) =
    getInputTyping(key).getClassName(coords.owner.toKotlinPackageName(), coords.buildActionClassName())
        .copy(nullable = !input.shouldBeNonNullInWrapper())