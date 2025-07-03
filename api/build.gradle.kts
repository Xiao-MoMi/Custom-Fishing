plugins {
    id("maven-publish")
}

repositories {
    maven("https://jitpack.io/") // rtag
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // papi
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.momirealms.net/releases/")
}

dependencies {
    implementation(files("libs/boosted-yaml-${rootProject.properties["boosted_yaml_version"]}.jar"))
    implementation("com.saicone.rtag:rtag:${rootProject.properties["rtag_version"]}")
    implementation("com.saicone.rtag:rtag-item:${rootProject.properties["rtag_version"]}")
    compileOnly("dev.folia:folia-api:${rootProject.properties["paper_version"]}-R0.1-SNAPSHOT")
    compileOnly("com.google.code.gson:gson:${rootProject.properties["gson_version"]}")
    compileOnly("me.clip:placeholderapi:${rootProject.properties["placeholder_api_version"]}")
    compileOnly("net.momirealms:sparrow-heart:${rootProject.properties["sparrow_heart_version"]}")
    compileOnly("org.incendo:cloud-core:${rootProject.properties["cloud_core_version"]}")
    compileOnly("org.incendo:cloud-minecraft-extras:${rootProject.properties["cloud_minecraft_extras_version"]}")
    compileOnly("org.jetbrains:annotations:${rootProject.properties["jetbrains_annotations_version"]}")
    compileOnly("org.slf4j:slf4j-api:${rootProject.properties["slf4j_version"]}")
    compileOnly("org.apache.logging.log4j:log4j-core:${rootProject.properties["log4j_version"]}")
    compileOnly("net.kyori:adventure-text-minimessage:${rootProject.properties["adventure_bundle_version"]}")
    compileOnly("net.kyori:adventure-text-serializer-gson:${rootProject.properties["adventure_bundle_version"]}")
    compileOnly("net.kyori:adventure-text-serializer-json-legacy-impl:${rootProject.properties["adventure_bundle_version"]}")
    compileOnly("com.github.ben-manes.caffeine:caffeine:${rootProject.properties["caffeine_version"]}")
    compileOnly("net.objecthunter:exp4j:${rootProject.properties["exp4j_version"]}")
    compileOnly("com.google.guava:guava:${rootProject.properties["guava_version"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
    dependsOn(tasks.clean)
}

tasks {
    shadowJar {
        archiveClassifier = ""
        archiveFileName = "custom-fishing-${rootProject.properties["project_version"]}.jar"
        relocate("net.kyori", "net.momirealms.customfishing.libraries")
        relocate("dev.dejvokep", "net.momirealms.customfishing.libraries")
        relocate("com.saicone.rtag", "net.momirealms.customfishing.libraries.rtag")
    }
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.momirealms.net/releases")
            credentials(PasswordCredentials::class) {
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "net.momirealms"
            artifactId = "custom-fishing"
            version = rootProject.properties["project_version"].toString()
            artifact(tasks["sourcesJar"])
            from(components["shadow"])
            pom {
                name = "CustomFishing API"
                url = "https://github.com/Xiao-MoMi/Custom-Fishing"
                licenses {
                    license {
                        name = "GNU General Public License v3.0"
                        url = "https://www.gnu.org/licenses/gpl-3.0.html"
                        distribution = "repo"
                    }
                }
            }
        }
    }
}

