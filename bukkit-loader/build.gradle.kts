repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":common"))
    implementation(project(":compatibility"))
    implementation(project(":api"))
    compileOnly("org.spigotmc:spigot-api:${rootProject.properties["paper_version"]}-R0.1-SNAPSHOT")
}
