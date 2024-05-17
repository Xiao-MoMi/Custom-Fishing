plugins {
    id("io.github.goooler.shadow") version "8.1.7"
}

repositories {
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://jitpack.io/")
}

dependencies {
    implementation(project(":common"))
    implementation("dev.dejvokep:boosted-yaml:${rootProject.properties["boosted_yaml_version"]}")
    implementation("net.kyori:adventure-api:${rootProject.properties["adventure_bundle_version"]}") {
        exclude(module = "adventure-bom")
        exclude(module = "checker-qual")
        exclude(module = "annotations")
    }
    implementation("com.saicone.rtag:rtag:1.5.3")
    implementation("com.saicone.rtag:rtag-item:1.5.3")
    compileOnly("dev.folia:folia-api:${rootProject.properties["paper_version"]}-R0.1-SNAPSHOT")
    compileOnly("com.google.code.gson:gson:${rootProject.properties["gson_version"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
    dependsOn(tasks.clean)
}


tasks {
    shadowJar {
        relocate("net.kyori", "net.momirealms.customfishing.libraries")
        relocate("dev.dejvokep", "net.momirealms.customfishing.libraries")
    }
}