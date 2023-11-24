dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    implementation("de.tr7zw:item-nbt-api:2.12.1")
}

tasks {
    shadowJar {
        relocate ("de.tr7zw.changeme", "net.momirealms.customfishing.libraries")
        relocate ("de.tr7zw.annotations", "net.momirealms.customfishing.libraries.annotations")
    }
}
