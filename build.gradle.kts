plugins {
    kotlin("js") version "1.3.61"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-js"))
}

kotlin.target {
    browser { }
}

//val mainSourceSet = kotlin.sourceSets.onEach { println("sourceset"+it) }["main"]!!

tasks {

    compileKotlinJs {
        kotlinOptions {
//            outputFile = "output.js"
            sourceMap = true
            moduleKind = "umd"
        }
    }


    val unpackKotlinJsStdlib by creating {
        group = "build"
        description = "Unpack the Kotlin JavaScript standard library"
        val outputDir = file("$buildDir/$name")
        val compileClasspath = configurations["compileClasspath"]
        inputs.property("compileClasspath", compileClasspath)
        outputs.dir(outputDir)
        doLast {
            val kotlinStdLibJar = compileClasspath.single {
                it.name.matches(Regex("kotlin-stdlib-js-.+\\.jar"))
            }
            copy {
                includeEmptyDirs = false
                from(zipTree(kotlinStdLibJar))
                into(outputDir)
                include("**/*.js")
                exclude("META-INF/**")
            }
        }
    }
    val assembleWeb by creating(Copy::class) {
        group = "build"
        description = "Assemble the web application"
        includeEmptyDirs = false         
        from(unpackKotlinJsStdlib)
        from(compileKotlinJs.get().outputFile.parent) {
            exclude("**/*.kjsm")
        }
        from(processResources.get().destinationDir)
        into("$buildDir/web")
    }
    assemble {
        dependsOn(assembleWeb)
    }
}

