plugins {
    kotlin("multiplatform") version "1.3.70-eap-42"
}

repositories {
    jcenter()
    mavenCentral()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
}

val versions by extra {
    mapOf(
        "html" to "0.6.12",
        "coroutines" to "1.3.3"
    )
}

kotlin {
    js {
        val main by compilations.getting {
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs +
                        "-Xuse-experimental=kotlinx.coroutines.FlowPreview" +
                        "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi"
            }
        }

        browser {
            webpackTask {
                this.sourceMaps = true
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-html-common:${versions["html"]}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:${versions["coroutines"]}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:${versions["html"]}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:${versions["coroutines"]}")
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }


    }

}
