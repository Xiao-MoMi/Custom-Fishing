dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    compileOnly("de.tr7zw:item-nbt-api:2.12.4")
}

tasks {
    shadowJar {
        relocate ("de.tr7zw.changeme", "net.momirealms.customfishing.libraries")
    }
}
