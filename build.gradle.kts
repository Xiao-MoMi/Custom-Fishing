plugins {
    id("java")
    id("application")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

allprojects {

    version = "2.0-beta"

    apply<JavaPlugin>()
    apply(plugin = "java")
    apply(plugin = "application")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "org.gradle.maven-publish")

    application {
        mainClass.set("")
    }

    repositories {
        maven("https://maven.aliyun.com/repository/public/")
        mavenCentral()
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://maven.enginehub.org/repo/")
        maven("https://jitpack.io/")
        maven("https://mvn.lumine.io/repository/maven-public/")
        maven("https://repo.rapture.pw/repository/maven-releases/")
        maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
        maven("https://r.irepo.space/maven/")
        maven("https://repo.auxilor.io/repository/maven-public/")
        maven("https://betonquest.org/nexus/repository/betonquest/")
        maven("https://repo.william278.net/releases/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

subprojects {
    tasks.processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.shadowJar {
        destinationDirectory.set(file("$rootDir/target"))
        archiveClassifier.set("")
        archiveFileName.set("CustomFishing-" + project.name + "-" + project.version + ".jar")
    }

//    tasks.javadoc.configure {
//        options.quiet()
//    }
//
//    if ("api" == project.name) {
//        java {
//            withSourcesJar()
//            withJavadocJar()
//        }
//    }
}