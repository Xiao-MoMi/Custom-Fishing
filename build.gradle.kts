import java.io.ByteArrayOutputStream

plugins {
    id("java")
}

val commitID : String = versionBanner()
ext["commitID"] = commitID

subprojects {

    apply(plugin = "java")
    apply(plugin = "java-library")

    repositories {
        mavenCentral()
    }

    tasks.processResources {
        filteringCharset = "UTF-8"

        filesMatching(arrayListOf("library-version.properties")) {
            expand(rootProject.properties)
        }

        filesMatching(arrayListOf("plugin.yml", "*.yml", "*/*.yml")) {
            expand(
                Pair("git_version", commitID),
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