dependencies {
    // server
    compileOnly("dev.folia:folia-api:1.20.4-R0.1-SNAPSHOT")

    // packet
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")

    // command
    compileOnly("dev.jorel:commandapi-bukkit-core:9.4.1")

    // bStats
    compileOnly("org.bstats:bstats-bukkit:3.0.2")

    // papi
    compileOnly("me.clip:placeholderapi:2.11.5")

    // config
    compileOnly("dev.dejvokep:boosted-yaml:1.3.4")

    // mythic
    compileOnly("io.lumine:Mythic-Dist:5.3.5")
    compileOnly("net.Indyuce:MMOItems-API:6.9.2-SNAPSHOT")
    compileOnly("io.lumine:MythicLib-dist:1.6-SNAPSHOT")
    compileOnly("net.Indyuce:MMOCore-API:1.12-SNAPSHOT")

    // Gson
    compileOnly("com.google.code.gson:gson:2.10.1")

    // eco
    compileOnly("com.willfp:eco:6.67.2")
    compileOnly("com.willfp:EcoJobs:3.47.1")
    compileOnly("com.willfp:EcoSkills:3.21.0")
    compileOnly("com.willfp:libreforge:4.48.1")

    // database
    compileOnly("org.xerial:sqlite-jdbc:3.45.3.0")
    compileOnly("com.h2database:h2:2.2.224")
    compileOnly("org.mongodb:mongodb-driver-sync:5.0.1")
    compileOnly("com.zaxxer:HikariCP:5.0.1")
    compileOnly("redis.clients:jedis:5.1.2")

    // others
    compileOnly("com.github.LoneDev6:api-itemsadder:3.5.0c-r5")
    compileOnly("io.th0rgal:oraxen:1.165.0")
    compileOnly("pers.neige.neigeitems:NeigeItems:1.16.24")
    compileOnly("com.github.Zrips:Jobs:4.17.2")
    compileOnly("com.github.Archy-X:AureliumSkills:Beta1.3.21")
    compileOnly("dev.aurelium:auraskills-api-bukkit:2.0.0-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("org.betonquest:betonquest:2.0.0")
    compileOnly("com.github.Xiao-MoMi:Custom-Crops:3.4.4.1")
    compileOnly("org.apache.commons:commons-lang3:3.14.0")

    // local jars
    compileOnly(files("libs/AdvancedEnchantments-api.jar"))
    compileOnly(files("libs/BattlePass-4.0.6-api.jar"))
    compileOnly(files("libs/RealisticSeasons-api.jar"))
    compileOnly(files("libs/mcMMO-api.jar"))
    compileOnly(files("libs/ClueScrolls-4.8.7-api.jar"))
    compileOnly(files("libs/notquests-5.17.1.jar"))
    compileOnly(files("libs/zaphkiel-2.0.24.jar"))

    // GUI
    implementation("xyz.xenondevs.invui:invui:1.30") {
        exclude("org.jetbrains", "annotations")
    }

    // nbt
    implementation("de.tr7zw:item-nbt-api:2.12.4")

    // api module
    implementation(project(":api"))

    // sparrow heart
    implementation("com.github.Xiao-MoMi:Sparrow-Heart:0.16")

    // adventure
    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("net.kyori:adventure-text-serializer-gson:4.17.0") {
        exclude("com.google.code.gson", "gson")
    }
    implementation("net.kyori:adventure-platform-bukkit:4.3.2")
}

tasks {
    shadowJar {
        exclude("org.jetbrains:annotations:*")
        relocate ("org.apache.commons.pool2", "net.momirealms.customfishing.libraries.commonspool2")
        relocate ("org.apache.commons.lang3", "net.momirealms.customfishing.libraries.lang3")
        relocate ("com.mysql", "net.momirealms.customfishing.libraries.mysql")
        relocate ("org.mariadb", "net.momirealms.customfishing.libraries.mariadb")
        relocate ("com.zaxxer.hikari", "net.momirealms.customfishing.libraries.hikari")
        relocate ("redis.clients.jedis", "net.momirealms.customfishing.libraries.jedis")
        relocate ("com.mongodb", "net.momirealms.customfishing.libraries.mongodb")
        relocate ("org.bson", "net.momirealms.customfishing.libraries.bson")
        relocate ("net.objecthunter.exp4j", "net.momirealms.customfishing.libraries.exp4j")
        relocate ("de.tr7zw.changeme", "net.momirealms.customfishing.libraries.changeme")
        relocate ("net.kyori", "net.momirealms.customfishing.libraries")
        relocate ("dev.jorel.commandapi", "net.momirealms.customfishing.libraries.commandapi")
        relocate ("dev.dejvokep.boostedyaml", "net.momirealms.customfishing.libraries.boostedyaml")
        relocate ("org.bstats", "net.momirealms.customfishing.libraries.bstats")
        relocate ("net.momirealms.sparrow.heart", "net.momirealms.customfishing.libraries.heart")
        relocate ("xyz.xenondevs", "net.momirealms.customfishing.libraries")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
