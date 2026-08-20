plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.kotlinSerialization)
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("maven-publish")
    alias(libs.plugins.sqldelight)
}

// JitPack runs on Linux — skip slow iOS native compile for fast publishes (~3-6 min).
// iOS host apps still compile iOS code locally via Kotlin/Native in their own project.
val skipIosTargets =
    project.findProperty("corecmp.skip.ios") == "true" ||
        System.getenv("CORECMP_SKIP_IOS") == "true" ||
        System.getenv("JITPACK") == "true"

group = "com.corecmp"

version = findProperty("corecmp.version")?.toString() ?: "1.0.0"

kotlin {
    androidLibrary {
        namespace = "com.corecmp.shared"
        compileSdk = 36
        minSdk = 24

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }

        androidResources {
            enable = true
        }
    }

    jvm()

    if (!skipIosTargets) {
        iosArm64()
        iosSimulatorArm64()
    }

    sourceSets {
        commonMain.dependencies {
            // Core
            api(libs.kotlin.stdlib)
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.datetime)

            // Compose
            api(libs.runtime)
            api(libs.foundation)
            api(libs.material3)
            api(libs.compose.components.resources)
            api(libs.material.icons.core)

            // DI
            api(libs.koin.core)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)

            // Networking (exported so consumer doesn't need to add)
            api(libs.ktor.client.core)
            api(libs.ktor.client.content.negotiation)
            api(libs.bundles.ktor)

            // Media / UI utils
            api(libs.compose.lottie)
            api(libs.bundles.coil)

            // Settings
            api(libs.multiplatform.settings.core)
            api(libs.multiplatform.settings.serialization)

            // Lifecycle (JetBrains KMP wrappers)
            api(libs.jetbrains.lifecycle.viewmodel)
            api(libs.jetbrains.lifecycle.runtime)

            // Navigation (JetBrains KMP wrappers)
            api(libs.navigation.compose)

            // Database
            api(libs.sqldelight.coroutines.extensions)
        }

        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.client.cio)
            implementation(libs.webcam.capture)
            implementation(libs.pdfbox)
            implementation(libs.kotlinx.datetime)
            implementation(libs.sqldelight.sqlite.driver)
        }

        commonTest.dependencies {
            implementation("org.jetbrains.kotlin:kotlin-test")
        }

        if (!skipIosTargets) {
            nativeMain.dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.sqldelight.native.driver)
            }
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.security.crypto)
            implementation(libs.androidx.biometric)
            implementation(libs.play.app.update)
            implementation(libs.play.review)
            implementation(libs.play.services.auth)
            implementation(libs.zxing.core)
            implementation(libs.sqldelight.android.driver)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.corecmp.shared.generated.resources"
}

sqldelight {
    databases {
        create("CoreCmpDatabase") {
            packageName.set("com.corecmp.shared.db")
        }
    }
}

tasks.withType<Copy>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

configurations.configureEach {
    if (name.contains("jvm", ignoreCase = true) && (isCanBeResolved || isCanBeConsumed)) {
        attributes {
            attribute(
                org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.attribute,
                org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
            )
        }
    }
}

publishing {
    repositories {
        // Free: artifacts committed to repo by GitHub Actions (see .github/workflows/publish.yml)
        maven {
            url = uri("${rootProject.projectDir}/maven-repo")
        }
    }
    publications.withType<MavenPublication> {
        pom {
            name.set("CoreCmp")
            description.set("Kotlin Multiplatform toolkit — API, UI, permissions, upload, security")
            url.set("https://github.com/deepakchoudhary/CoreCMP")
            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("deepakchoudhary")
                    name.set("Deepak Choudhary")
                    email.set("deepak.choudhary@example.com")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/deepakchoudhary/CoreCMP.git")
                developerConnection.set("scm:git:ssh://github.com:deepakchoudhary/CoreCMP.git")
                url.set("https://github.com/deepakchoudhary/CoreCMP")
            }
        }
    }
}

// Skip Android Lint on JitPack only — use exact task names (never match *Metadata* tasks).
if (skipIosTargets) {
    val jitPackLintTasks = setOf(
        "lint",
        "lintAnalyzeAndroidMain",
        "lintReportAndroidMain",
        "prepareLintJarForPublish",
    )
    tasks.matching { it.project == project && it.name in jitPackLintTasks }.configureEach {
        enabled = false
    }
}