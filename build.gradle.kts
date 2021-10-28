import java.text.SimpleDateFormat
import java.util.Date

val mainClass: String = "com.github.lure0xaos.L0X"

val projectName: String by gradle.rootProject
val projectGroup: String by gradle.rootProject
val projectVersion: String by gradle.rootProject
val projectDescription: String by gradle.rootProject

group = projectGroup
version = projectVersion
description = projectDescription

extra["projectBuild"] = SimpleDateFormat("yyyyMMddHHmmss").format(Date())

plugins {
  kotlin("jvm") version "1.7.10"
  id("io.freefair.sass-java") version "6.5.0-rc1"
  id("io.freefair.sass-webjars") version "6.5.0-rc1"
}

repositories { mavenCentral() }

@Suppress("SpellCheckingInspection")
dependencies {
  implementation("org.thymeleaf:thymeleaf:3.0.15.RELEASE")
  implementation("org.thymeleaf.extras:thymeleaf-extras-java8time:3.0.4.RELEASE")
  implementation("com.vladsch.flexmark:flexmark:0.64.0")



  implementation("org.slf4j:slf4j-api:1.7.36")
  implementation("org.slf4j:slf4j-jdk-platform-logging:2.0.0-alpha5")
  implementation("ch.qos.logback:logback-classic:1.2.11")
  implementation("org.slf4j:slf4j-simple:2.0.0-alpha5")

  implementation("org.webjars:bootstrap:5.1.3")
  implementation("org.webjars.npm:bootstrap-icons:1.9.1")
}

tasks.processResources {
  duplicatesStrategy = DuplicatesStrategy.WARN
  with(copySpec()).filesMatching("**/*.properties") {
    filter {
      (project.properties + project.ext.properties).entries.fold(it) { line, (key, value) ->
        line.replace("@$key@", value.toString())
      }
    }
    duplicatesStrategy = DuplicatesStrategy.WARN
  }
}

tasks.compileKotlin {
  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_16.toString()
  }
}
val fatJar: Jar = task(name = "fatJar", type = Jar::class) {
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
