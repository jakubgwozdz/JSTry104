plugins {
    kotlin("multiplatform") version "1.3.61"
}

kotlin {
    js {
        browser { }
    }
}

repositories {
    jcenter()
    mavenCentral()
}

kotlin.sourceSets["commonMain"].dependencies {
    implementation(kotlin("stdlib-common"))
    implementation("org.jetbrains.kotlinx:kotlinx-html-common:0.6.12")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.3")
}

kotlin.sourceSets["jsMain"].dependencies {
    implementation(kotlin("stdlib-js"))
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.6.12")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.3")
}

//tasks {
//
//    compileKotlinJs {
//        kotlinOptions {
//            sourceMap = true
//            sourceMapEmbedSources = "always"
//            sourceMapPrefix = "../../"
//            moduleKind = "umd"
//            verbose = true
//            metaInfo = false
//        }
//    }
//
//    val unpackLibraries by creating {
//        group = "build"
//        description = "Unpack the libraries"
//        val outputDir = file("$buildDir/$name")
//        val compileClasspath = configurations["compileClasspath"]
//        inputs.property("compileClasspath", compileClasspath)
//        outputs.dir(outputDir)
//        doLast {
//            compileClasspath.forEach {
//                it.name.matches(Regex("kotlin-stdlib-js-.+\\.jar"))
//                copy {
//                    includeEmptyDirs = false
//                    from(zipTree(it))
//                    into(outputDir)
//                    exclude("**/*.kotlin_metadata")
//                    exclude("**/*.meta.js")
//                    exclude("META-INF/**")
//                    exclude("**/*.kjsm")
//                }
//            }
//        }
//    }
//
//    val assembleWeb by creating(Copy::class) {
//        group = "build"
//        description = "Assemble the web application"
//        includeEmptyDirs = false
//        from(unpackLibraries)
//        from(compileKotlinJs.get().outputFile.parent)
//        from(processResources.get().destinationDir)
//        exclude("**/*.kotlin_metadata")
//        exclude("**/*.meta.js")
//        exclude("META-INF/**")
//        exclude("**/*.kjsm")
//        into("$buildDir/web")
//    }
//
//    assemble {
//        dependsOn(assembleWeb)
//    }
//}

