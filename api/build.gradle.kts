dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0")
    implementation("de.tr7zw:item-nbt-api:2.11.3")
}

tasks {
    shadowJar {
        relocate ("de.tr7zw", "net.momirealms.customfishing.libraries")
    }
}
