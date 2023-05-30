rootProject.name = "github-workflows-kt"

apply(from = "./buildSrc/repositories.settings.gradle.kts")

include(
    "library",
    ":kotlin2yaml:logic",
    ":kotlin2yaml:web",
    ":kotlin2yaml:web:api",
    ":kotlin2yaml:web:ui",
    ":automation:typings",
    ":automation:wrapper-generator",
)

plugins {
    id("com.gradle.enterprise") version "3.13.2"
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage") // Central declaration of repositories is an incubating feature
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishAlwaysIf(System.getenv("GITHUB_ACTIONS") == "true")
        publishOnFailure()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
