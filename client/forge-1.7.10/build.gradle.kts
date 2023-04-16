import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.RunConfigurationContainer

buildscript {
  repositories {
    maven {
      url = uri("https://plugins.gradle.org/m2/")
    }
  }
  dependencies {
    classpath("com.github.johnrengelman:shadow:8.1.1")
  }
}

apply(plugin = "com.github.johnrengelman.shadow")

plugins {
  id("com.gtnewhorizons.retrofuturagradle") version "1.2.3"
  id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
  id("eclipse")
}



java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
    vendor.set(JvmVendorSpec.AZUL)
  }
  withSourcesJar()
  withJavadocJar()
}

configurations {
  register("jarJar") {
    extendsFrom(implementation.get())
    isCanBeResolved = true
  }
}

group = "club.moddedminecraft.polychat.client"
version = "2.0.2"

repositories {
    mavenCentral()
    mavenLocal()
}

var jarJar = configurations.named("jarJar").get()

dependencies {
  compileOnly(files("libs/client-base-${version}.jar"))
  compileOnly(files("libs/message-library-${version}.jar"))
  compileOnly(files("libs/network-library-${version}.jar"))
  compileOnly(files("libs/common-${version}.jar"))
  compileOnly("com.google.protobuf:protobuf-java:3.16.3")
  compileOnly("org.yaml:snakeyaml:1.+")
  jarJar(files("libs/message-library-${version}.jar"))
  jarJar(files("libs/network-library-${version}.jar"))
  jarJar(files("libs/common-${version}.jar"))
  jarJar(files("libs/client-base-${version}.jar"))
  jarJar("com.google.protobuf:protobuf-java:3.16.3")
  jarJar("org.yaml:snakeyaml:1.+")
}

minecraft {
  mcVersion.set("1.7.10")
}

// IDE Settings
eclipse {
  classpath {
    isDownloadSources = true
    isDownloadJavadoc = true
  }
}

idea {
  module {
    isDownloadJavadoc = true
    isDownloadSources = true
    inheritOutputDirs = true // Fix resources in IJ-Native runs
  }
  project {
    this.withGroovyBuilder {
      "settings" {
        "runConfigurations" {
          val self = this.delegate as RunConfigurationContainer
          self.add(Gradle("1. Run Client").apply {
            setProperty("taskNames", listOf("runClient"))
          })
          self.add(Gradle("2. Run Server").apply {
            setProperty("taskNames", listOf("runServer"))
          })
          self.add(Gradle("3. Run Obfuscated Client").apply {
            setProperty("taskNames", listOf("runObfClient"))
          })
          self.add(Gradle("4. Run Obfuscated Server").apply {
            setProperty("taskNames", listOf("runObfServer"))
          })
        }
        "compiler" {
          val self = this.delegate as org.jetbrains.gradle.ext.IdeaCompilerConfiguration
          afterEvaluate {
            self.javac.moduleJavacAdditionalOptions = mapOf(
              (project.name + ".main") to
                tasks.compileJava.get().options.compilerArgs.map { '"' + it + '"' }.joinToString(" ")
            )
          }
        }
      }
    }
  }
}

tasks {
  jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.named("jarJar").get()
      .onEach { println("add from dependencies: ${it.name}") }
      .map { if (it.isDirectory) it else zipTree(it) })
    val sourcesMain = sourceSets.main.get()
    sourcesMain.allSource.forEach { println("add from sources: ${it.name}") }
    from(sourcesMain.output)
  }
}
