@file:Suppress("SpellCheckingInspection")

import org.jetbrains.kotlin.gradle.utils.extendsFrom
//import sass.embedded_protocol.EmbeddedSass
import java.text.SimpleDateFormat
import java.util.*

val mainClass: String = "com.github.lure0xaos.L0X"

val projectName: String by project
val projectGroup: String by project
val projectVersion: String by project
val projectDescription: String by project

group = projectGroup
version = projectVersion
description = projectDescription

extra["projectBuild"] = SimpleDateFormat("yyyyMMddHHmmss").format(Date())

plugins {
    kotlin("jvm") version "2.0.20"
    id("io.freefair.sass-java") version "8.10"
    id("io.freefair.sass-webjars") version "8.10"
    application
}

repositories { mavenCentral() }

val webjarExplode: Configuration by configurations.creating { isTransitive = false }

configurations.implementation.extendsFrom(configurations.named("webjarExplode"))

dependencies {
    implementation("org.thymeleaf:thymeleaf:3.1.2.RELEASE")
    implementation("org.thymeleaf.extras:thymeleaf-extras-java8time:3.0.4.RELEASE")
    implementation("com.vladsch.flexmark:flexmark:0.64.8")

    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("org.slf4j:slf4j-jdk-platform-logging:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.5")
//    implementation("org.slf4j:slf4j-simple:2.0.7")

    webjarExplode("org.webjars.npm:bootstrap:5.3.3")
    webjarExplode("org.webjars.npm:bootstrap-icons:1.11.3")
}

kotlin {
    jvmToolchain(21)
}

val webjarDir: File =
    layout.buildDirectory.dir("resources/main/static/webjars").get().asFile

val unzipWebjars: Sync = tasks.create<Sync>("unzipWebjars") {
    duplicatesStrategy = DuplicatesStrategy.WARN
    webjarExplode.files.forEach { jar ->
        val artifact =
            webjarExplode.resolvedConfiguration.resolvedArtifacts.first { it.file.toString() == jar.absolutePath }
        val moduleVersion = artifact.moduleVersion.id.version
        val upStreamVersion = moduleVersion.replace(Regex("(-[\\d.-]+)"), "")
        copy {
            val root = "META-INF/resources/webjars/${artifact.name}"
            val upStreamRoot = "$root/$upStreamVersion"
            val moduleRoot = "$root/$moduleVersion"
            from(zipTree(jar).matching { include("$upStreamRoot/**", "$moduleRoot/**") })
            eachFile { relativePath = RelativePath(true, relativePath.pathString.removePrefix("$upStreamRoot/")) }
            into("$webjarDir/${artifact.name}")
        }
    }
}

tasks.processResources {
    dependsOn(unzipWebjars)
    duplicatesStrategy = DuplicatesStrategy.WARN
    with(copySpec()).filesMatching("**/*.properties") {
        val entries = (project.properties + project.ext.properties).entries
        filter {
            entries.fold(it) { line, (key, value) -> line.replace("@$key@", value.toString()) }
        }
        duplicatesStrategy = DuplicatesStrategy.WARN
    }
}

@Suppress("UnstableApiUsage")
sass {
    omitSourceMapUrl.set(false)
//    outputStyle.set(EmbeddedSass.OutputStyle.EXPANDED)
    sourceMapContents.set(false)
    sourceMapEmbed.set(false)
    sourceMapEnabled.set(true)
}

tasks.compileSass {
    dependsOn(unzipWebjars)
    include(webjarDir.toString())
}

val fatJar: Jar = tasks.create<Jar>("fatJar") {
    manifest {
        attributes(
            "Implementation-Title" to project.displayName,
            "Implementation-Version" to archiveVersion,
            "Main-Class" to mainClass
        )
    }
    archiveBaseName.set("${project.name}-all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks.build { dependsOn(fatJar) }

application {
    mainClass = "com.github.lure0xaos.L0X"
}
