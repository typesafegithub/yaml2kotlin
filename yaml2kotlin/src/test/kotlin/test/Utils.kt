package test

import io.github.typesafegithub.workflows.scriptgenerator.rootProject
import java.io.File

val samples = rootProject.resolve("samples")

val sampleFiles: List<File> = samples.listFiles()
    ?.filter { it.extension in Conventions.yamlSuffixes }
    ?: emptyList()

fun isGitHubActions() =
    System.getenv("GITHUB_ACTIONS") == "true"

fun String.removeWindowsEndings(): String =
    replace("\r\n", "\n")
