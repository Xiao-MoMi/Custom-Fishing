plugins {
    id("io.github.goooler.shadow") version "8.1.8"
}

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // papi
    maven("https://libraries.minecraft.net") // brigadier
    maven("https://jitpack.io/") // sparrow-heart, rtag
    maven("https://papermc.io/repo/repository/maven-public/") // paper
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // spigot
}

dependencies {
    // platform
    compileOnly("dev.folia:folia-api:${rootProject.properties["paper_version"]}-R0.1-SNAPSHOT")
    // subprojects
    implementation(project(":common"))
    implementation(project(":api")) {
        exclude("dev.dejvokep", "boosted-yaml")
    }
    implementation(project(":compatibility"))
    // adventure
    implementation("net.kyori:adventure-api:${rootProject.properties["adventure_bundle_version"]}")
    implementation("net.kyori:adventure-text-minimessage:${rootProject.properties["adventure_bundle_version"]}")
    implementation("net.kyori:adventure-platform-bukkit:${rootProject.properties["adventure_platform_version"]}")
    implementation("net.kyori:adventure-text-serializer-gson:${rootProject.properties["adventure_bundle_version"]}") {
        exclude("com.google.code.gson", "gson")
    }
    // tag & component
    implementation("com.saicone.rtag:rtag:${rootProject.properties["rtag_version"]}")
    implementation("com.saicone.rtag:rtag-item:${rootProject.properties["rtag_version"]}")
    // nms util
    implementation("com.github.Xiao-MoMi:Sparrow-Heart:${rootProject.properties["sparrow_heart_version"]}")
    // bstats
    compileOnly("org.bstats:bstats-bukkit:${rootProject.properties["bstats_version"]}")
    // config
    compileOnly("dev.dejvokep:boosted-yaml:${rootProject.properties["boosted_yaml_version"]}")
    // serialization
    compileOnly("com.google.code.gson:gson:${rootProject.properties["gson_version"]}")
    // database
    compileOnly("org.xerial:sqlite-jdbc:${rootProject.properties["sqlite_driver_version"]}")
    compileOnly("com.h2database:h2:${rootProject.properties["h2_driver_version"]}")
    compileOnly("org.mongodb:mongodb-driver-sync:${rootProject.properties["mongodb_driver_version"]}")
    compileOnly("com.zaxxer:HikariCP:${rootProject.properties["hikari_version"]}")
    compileOnly("redis.clients:jedis:${rootProject.properties["jedis_version"]}")
    // cloud command framework
    compileOnly("org.incendo:cloud-core:${rootProject.properties["cloud_core_version"]}")
    compileOnly("org.incendo:cloud-minecraft-extras:${rootProject.properties["cloud_minecraft_extras_version"]}")
    compileOnly("org.incendo:cloud-paper:${rootProject.properties["cloud_paper_version"]}")
    // brigadier
    compileOnly("com.mojang:brigadier:${rootProject.properties["mojang_brigadier_version"]}")
    // expression
    compileOnly("net.objecthunter:exp4j:${rootProject.properties["exp4j_version"]}")
    // placeholder api
    compileOnly("me.clip:placeholderapi:${rootProject.properties["placeholder_api_version"]}")
    // lz4
    compileOnly("org.lz4:lz4-java:${rootProject.properties["lz4_version"]}")
}

tasks {
    shadowJar {
        archiveFileName = "CustomFishing-${rootProject.properties["project_version"]}.jar"
        destinationDirectory.set(file("$rootDir/target"))
        relocate("net.kyori", "net.momirealms.customfishing.libraries")
        relocate("org.incendo", "net.momirealms.customfishing.libraries")
        relocate("dev.dejvokep", "net.momirealms.customfishing.libraries")
        relocate("org.apache.commons.pool2", "net.momirealms.customfishing.libraries.commonspool2")
        relocate("com.mysql", "net.momirealms.customfishing.libraries.mysql")
        relocate("org.mariadb", "net.momirealms.customfishing.libraries.mariadb")
        relocate("com.zaxxer.hikari", "net.momirealms.customfishing.libraries.hikari")
        relocate("com.mongodb", "net.momirealms.customfishing.libraries.mongodb")
        relocate("org.bson", "net.momirealms.customfishing.libraries.bson")
        relocate("org.bstats", "net.momirealms.customfishing.libraries.bstats")
        relocate("com.github.benmanes.caffeine", "net.momirealms.customfishing.libraries.caffeine")
        relocate("net.momirealms.sparrow.heart", "net.momirealms.customfishing.bukkit.nms")
        relocate("com.saicone.rtag", "net.momirealms.customfishing.libraries.rtag")
        relocate("net.objecthunter.exp4j", "net.momirealms.customfishing.libraries.exp4j")
        relocate("net.jpountz", "net.momirealms.customfishing.libraries.jpountz") //lz4
        relocate("redis.clients.jedis", "net.momirealms.customfishing.libraries.jedis")
    }
}

artifacts {
    archives(tasks.shadowJar)
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