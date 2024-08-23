import java.io.ByteArrayOutputStream

plugins {
    id("java")
}

val git : String = versionBanner()
val builder : String = builder()
ext["git_version"] = git
ext["builder"] = builder

subprojects {

    apply(plugin = "java")
    apply(plugin = "java-library")

    repositories {
        mavenCentral()
        maven("https://jitpack.io/") // sparrow-heart, rtag
        maven("https://papermc.io/repo/repository/maven-public/") // paper
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // spigot
    }

    tasks.processResources {
        filteringCharset = "UTF-8"

        filesMatching(arrayListOf("custom-fishing.properties")) {
            expand(rootProject.properties)
        }

        filesMatching(arrayListOf("*.yml", "*/*.yml")) {
            expand(
                Pair("git_version", git),
                Pair("builder", builder),
                Pair("project_version", rootProject.properties["project_version"]),
                Pair("config_version", rootProject.properties["config_version"])
            )
        }
    }
}

fun versionBanner(): String {
    val os = ByteArrayOutputStream()
    project.exec {
        commandLine = "git rev-parse --short=8 HEAD".split(" ")
        standardOutput = os
    }
    return String(os.toByteArray()).trim()
}

fun builder(): String {
    val os = ByteArrayOutputStream()
    project.exec {
        commandLine = "git config user.name".split(" ")
        standardOutput = os
    }
    return String(os.toByteArray()).trim()
}