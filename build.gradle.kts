plugins {
    kotlin("multiplatform") version "1.3.70-eap-184"
}

repositories {
    jcenter()
    mavenCentral()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
}

val versions by extra {
    mapOf(
        "html" to "0.6.12",
        "coroutines" to "1.3.3",
        "assertj" to "3.11.1",
        "junit" to "5.5.2",
        "junit-console" to "1.5.2"
        )
}

kotlin {
    js {

        browser {
            webpackTask {
//                this.sourceMaps = true
            }
        }
    }

    jvm {
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-html-common:${versions["html"]}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:${versions["coroutines"]}")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:${versions["html"]}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:${versions["coroutines"]}")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions["coroutines"]}")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
//                implementation("org.assertj:assertj-core:${versions["assertj"]}")
//                implementation("org.junit.platform:junit-platform-console:${versions["junit-console"]}")
//                implementation("org.junit.jupiter:junit-jupiter-params:${versions["junit"]}")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:${versions["junit"]}")
            }
        }

        all {
            languageSettings.apply {
                languageVersion = "1.3"
                apiVersion = "1.3"
                useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
                useExperimentalAnnotation("kotlinx.coroutines.FlowPreview")
                useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }

    }

    tasks.getByName("jvmTest", Test::class) {
        useJUnitPlatform()
    }
}
