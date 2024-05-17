plugins {
    id("io.github.goooler.shadow") version "8.1.7"
}

dependencies {
    implementation(project(":common"))
    implementation(project(":api"))
    // adventure
    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("net.kyori:adventure-text-serializer-gson:4.17.0") {
        exclude("com.google.code.gson", "gson")
    }
    implementation("net.kyori:adventure-platform-bukkit:4.3.2")
    // GUI
    implementation("xyz.xenondevs.invui:invui:1.30") {
        exclude("org.jetbrains", "annotations")
    }
    // NBT
    implementation("com.saicone.rtag:rtag:1.5.3")
    implementation("com.saicone.rtag:rtag-item:1.5.3")
    // sparrow heart
    implementation("com.github.Xiao-MoMi:Sparrow-Heart:0.16")
    // server
    compileOnly("dev.folia:folia-api:1.20.4-R0.1-SNAPSHOT")
    // bStats
    compileOnly("org.bstats:bstats-bukkit:3.0.2")
    // papi
    compileOnly("me.clip:placeholderapi:2.11.5")
    // config
    compileOnly("dev.dejvokep:boosted-yaml:1.3.4")
    // Gson
    compileOnly("com.google.code.gson:gson:2.10.1")
    // database
    compileOnly("org.xerial:sqlite-jdbc:3.45.3.0")
    compileOnly("com.h2database:h2:2.2.224")
    compileOnly("org.mongodb:mongodb-driver-sync:5.0.1")
    compileOnly("com.zaxxer:HikariCP:5.0.1")
    compileOnly("redis.clients:jedis:5.1.2")
    // local jars
    compileOnly(files("libs/AdvancedEnchantments-api.jar"))
    compileOnly(files("libs/BattlePass-4.0.6-api.jar"))
    compileOnly(files("libs/RealisticSeasons-api.jar"))
    compileOnly(files("libs/mcMMO-api.jar"))
    compileOnly(files("libs/ClueScrolls-4.8.7-api.jar"))
    compileOnly(files("libs/notquests-5.17.1.jar"))
    compileOnly(files("libs/zaphkiel-2.0.24.jar"))
}

tasks {
    shadowJar {
        relocate("net.kyori", "net.momirealms.customfishing.libraries")
        relocate("org.incendo", "net.momirealms.customfishing.libraries")
        relocate("dev.dejvokep", "net.momirealms.customfishing.libraries")
        relocate("net.bytebuddy", "net.momirealms.customfishing.libraries.bytebuddy")
        relocate ("org.apache.commons.pool2", "net.momirealms.customfishing.libraries.commonspool2")
        relocate ("com.mysql", "net.momirealms.customfishing.libraries.mysql")
        relocate ("org.mariadb", "net.momirealms.customfishing.libraries.mariadb")
        relocate ("com.zaxxer.hikari", "net.momirealms.customfishing.libraries.hikari")
        relocate ("com.mongodb", "net.momirealms.customfishing.libraries.mongodb")
        relocate ("org.bson", "net.momirealms.customfishing.libraries.bson")
        relocate ("org.bstats", "net.momirealms.customfishing.libraries.bstats")
        relocate ("io.lettuce", "net.momirealms.customfishing.libraries.lettuce")
        relocate ("io.leangen.geantyref", "net.momirealms.customfishing.libraries.geantyref")
        relocate ("com.github.benmanes.caffeine", "net.momirealms.customfishing.libraries.caffeine")
        relocate ("net.momirealms.sparrow.heart", "net.momirealms.customfishing.bukkit.nms")
        relocate ("com.saicone.rtag", "net.momirealms.customfishing.libraries.rtag")
    }
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