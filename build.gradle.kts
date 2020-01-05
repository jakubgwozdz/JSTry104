plugins {
    kotlin("js") version "1.3.61"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-js"))
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.3")
}

kotlin.target {
    browser { }
}

tasks {

    compileKotlinJs {
        kotlinOptions {
            sourceMap = true
            sourceMapEmbedSources = "always"
            moduleKind = "umd"
        }
    }

    println("aaa " + compileKotlinJs.get().outputFile.parent)

    val unpackLibraries by creating {
        group = "build"
        description = "Unpack the libraries"
        val outputDir = file("$buildDir/$name")
        val compileClasspath = configurations["compileClasspath"]
        inputs.property("compileClasspath", compileClasspath)
        outputs.dir(outputDir)
        doLast {
            compileClasspath.forEach {
                it.name.matches(Regex("kotlin-stdlib-js-.+\\.jar"))
                copy {
                    includeEmptyDirs = false
                    from(zipTree(it))
                    into(outputDir)
                    exclude("**/*.kotlin_metadata")
                    exclude("**/*.meta.js")
                    exclude("META-INF/**")
                    exclude("**/*.kjsm")
                }
            }
        }
    }

    val assembleWeb by creating(Copy::class) {
        group = "build"
        description = "Assemble the web application"
        includeEmptyDirs = false
        from(unpackLibraries)
        from(compileKotlinJs.get().outputFile.parent)
        from(processResources.get().destinationDir)
        exclude("**/*.kotlin_metadata")
        exclude("**/*.meta.js")
        exclude("META-INF/**")
        exclude("**/*.kjsm")
        into("$buildDir/web")
    }

    assemble {
        dependsOn(assembleWeb)
    }
}

