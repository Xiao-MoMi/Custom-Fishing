dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":api"))
    // papi
    compileOnly("me.clip:placeholderapi:2.11.5")
    // server
    compileOnly("dev.folia:folia-api:1.20.4-R0.1-SNAPSHOT")

    // local jars
    compileOnly(files("libs/AdvancedEnchantments-api.jar"))
    compileOnly(files("libs/BattlePass-4.0.6-api.jar"))
    compileOnly(files("libs/RealisticSeasons-api.jar"))
    compileOnly(files("libs/mcMMO-api.jar"))
    compileOnly(files("libs/ClueScrolls-4.8.7-api.jar"))
    compileOnly(files("libs/notquests-5.17.1.jar"))
    compileOnly(files("libs/zaphkiel-2.0.24.jar"))
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