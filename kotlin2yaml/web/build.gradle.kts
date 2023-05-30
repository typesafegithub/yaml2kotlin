import io.ktor.plugin.features.DockerImageRegistry

plugins {
    kotlin("plugin.serialization")
    id("io.ktor.plugin") version "2.3.0"
    buildsrc.convention.`kotlin-jvm`
    application
}

dependencies {
    implementation(projects.kotlin2yaml.logic)
    implementation(projects.kotlin2yaml.web.api)

    implementation(platform("io.ktor:ktor-bom:2.3.0"))
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("org.slf4j:slf4j-simple:2.0.7")
}

val targetJsBundleDir = "${project.buildDir}/resources"

sourceSets {
    main {
        resources {
            setSrcDirs(srcDirs + targetJsBundleDir)
        }
    }
}

tasks.processResources {
    dependsOn(copyJsBundleToResources)
    dependsOn(tasks.processTestResources)
}

tasks["sourcesJar"].dependsOn(tasks.processResources)

val copyJsBundleToResources by tasks.registering(Copy::class) {
    dependsOn(":yaml2kotlin:web:ui:build")
    from("$rootDir/yaml2kotlin/web/ui/build/distributions")
    into(targetJsBundleDir)
}

application {
    mainClass.set("io.github.typesafegithub.workflows.scriptgenerator.rest.MainKt")
}

val dockerAppName = "github-workflows-kt-script-generator-server"

ktor {
    docker {
        localImageName.set(dockerAppName)
        imageTag.set(projects.library.version)

        externalRegistry.set(
            DockerImageRegistry.dockerHub(
                appName = provider { dockerAppName },
                username = providers.environmentVariable("DOCKERHUB_USERNAME"),
                password = providers.environmentVariable("DOCKERHUB_PASSWORD"),
            ),
        )
    }
}
