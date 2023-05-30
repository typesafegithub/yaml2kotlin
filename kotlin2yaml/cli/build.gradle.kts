plugins {
    buildsrc.convention.`kotlin-jvm`
    application
}

dependencies {
    implementation(projects.kotlin2yaml.logic)
}

application {
    mainClass.set("io.github.typesafegithub.workflows.scriptgenerator.MainKt")
    tasks.run.get().workingDir = rootProject.projectDir
}
