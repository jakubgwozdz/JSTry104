plugins {
    id("org.jetbrains.kotlin.js") version "1.3.61"
}

group = "eu.jakubgwozdz"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-js"))
}

kotlin.target.browser { }