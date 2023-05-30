plugins {
    kotlin("js")
    kotlin("plugin.serialization")

    id("org.jmailen.kotlinter")
}

dependencies {
    implementation(projects.kotlin2yaml.web.api)

    implementation(enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:1.0.0-pre.543"))

    implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-mui")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")

    implementation(platform("io.ktor:ktor-bom:2.3.0"))
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }
}
