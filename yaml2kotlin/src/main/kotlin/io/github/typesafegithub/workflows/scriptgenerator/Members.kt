package io.github.typesafegithub.workflows.scriptgenerator

import com.squareup.kotlinpoet.MemberName

object Members {
    val workflow = MemberName("$PACKAGE.dsl", "workflow")
    val linkedMapOf = MemberName("kotlin.collections", "linkedMapOf")
    val mapOf = MemberName("kotlin.collections", "mapOf")
    val listOf = MemberName("kotlin.collections", "listOf")
    val expr = MemberName("io.github.typesafegithub.workflows.dsl.expressions", "expr")
}
