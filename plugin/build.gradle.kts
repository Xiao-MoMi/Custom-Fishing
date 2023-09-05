dependencies {
    // server
    compileOnly("dev.folia:folia-api:1.20.1-R0.1-SNAPSHOT")

    // command
    compileOnly("dev.jorel:commandapi-bukkit-core:9.1.0")

    // packet
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0")

    // papi
    compileOnly("me.clip:placeholderapi:2.11.3")
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.1.0")

    // config
    compileOnly("dev.dejvokep:boosted-yaml:1.3.1")

    // mythic
    compileOnly("io.lumine:Mythic-Dist:5.3.5")
    compileOnly("net.Indyuce:MMOItems-API:6.9.2-SNAPSHOT")
    compileOnly("io.lumine:MythicLib-dist:1.6-SNAPSHOT")
    compileOnly("net.Indyuce:MMOCore-API:1.12-SNAPSHOT")

    // Gson
    compileOnly("com.google.code.gson:gson:2.10.1")

    // eco
    compileOnly("com.willfp:eco:6.65.4")
    compileOnly("com.willfp:EcoJobs:3.29.1")
    compileOnly("com.willfp:EcoSkills:3.17.1")
    compileOnly("com.willfp:libreforge:4.29.1")

    // database
    compileOnly("org.xerial:sqlite-jdbc:3.42.0.0")
    compileOnly("com.h2database:h2:2.2.220")
    compileOnly("org.mongodb:mongodb-driver-sync:4.10.2")
    compileOnly("com.zaxxer:HikariCP:5.0.1")
    compileOnly("redis.clients:jedis:4.4.3")

    // others
    compileOnly("com.github.LoneDev6:api-itemsadder:3.5.0b")
    compileOnly("com.github.oraxen:oraxen:1.159.0")
    compileOnly("pers.neige.neigeitems:NeigeItems:1.15.9")
    compileOnly("com.github.Zrips:Jobs:4.17.2")
    compileOnly("com.github.Archy-X:AureliumSkills:Beta1.3.21")

    // local jars
    compileOnly(files("libs/AdvancedEnchantments-api.jar"))
    compileOnly(files("libs/RealisticSeasons-api.jar"))
    compileOnly(files("libs/CustomCrops-api.jar"))
    compileOnly(files("libs/mcMMO-api.jar"))

    // api module
    implementation(project(":api"))

    // adventure
    implementation("net.kyori:adventure-api:4.14.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.0")
    implementation("net.kyori:adventure-text-minimessage:4.14.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.14.0")

    // nbt
    implementation("de.tr7zw:item-nbt-api:2.11.3")

    // bStats
    implementation("org.bstats:bstats-bukkit:3.0.1")

    // local lib
    implementation(files("libs/BiomeAPI.jar"))
    implementation(files("libs/ProtectionLib.jar"))

    // anvil
    implementation("net.wesjd:anvilgui:1.7.0-SNAPSHOT")
}

tasks {
    shadowJar {
        relocate ("de.tr7zw.changeme", "net.momirealms.customfishing.libraries")
        relocate ("de.tr7zw.annotations", "net.momirealms.customfishing.libraries.annotations")
        relocate ("net.kyori", "net.momirealms.customfishing.libraries")
        relocate ("net.wesjd", "net.momirealms.customfishing.libraries")
        relocate ("org.bstats", "net.momirealms.customfishing.libraries.bstats")
        relocate ("net.momirealms.biomeapi", "net.momirealms.customfishing.libraries.biomeapi")
        relocate ("net.momirealms.protectionlib", "net.momirealms.customfishing.libraries.protectionlib")
    }
}
