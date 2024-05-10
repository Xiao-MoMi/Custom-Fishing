/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package net.momirealms.customfishing.libraries.dependencies;

import com.google.common.collect.ImmutableList;
import net.momirealms.customfishing.libraries.dependencies.relocation.Relocation;
import org.bukkit.Bukkit;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

/**
 * The dependencies used by CustomFishing.
 */
public enum Dependency {

    ASM(
            "org.ow2.asm",
            "asm",
            "9.7",
            "asm"
    ),
    ASM_COMMONS(
            "org.ow2.asm",
            "asm-commons",
            "9.7",
            "asm-commons"
    ),
    JAR_RELOCATOR(
            "me.lucko",
            "jar-relocator",
            "1.7",
            "jar-relocator"
    ),
    COMMAND_API(
            "dev{}jorel",
            "commandapi-bukkit-shade",
            "9.4.1",
            "commandapi-bukkit",
            Relocation.of("commandapi", "dev{}jorel{}commandapi")
    ),
    COMMAND_API_MOJMAP(
            "dev{}jorel",
            "commandapi-bukkit-shade-mojang-mapped",
            "9.4.1",
            "commandapi-bukkit-shade-mojang-mapped",
            Relocation.of("commandapi", "dev{}jorel{}commandapi")
    ),
    MARIADB_DRIVER(
            "org{}mariadb{}jdbc",
            "mariadb-java-client",
            "3.3.3",
            "mariadb-java-client",
            Relocation.of("mariadb", "org{}mariadb")
    ),
    BOOSTED_YAML(
            "dev{}dejvokep",
            "boosted-yaml",
            "1.3.4",
            "boosted-yaml",
            Relocation.of("boostedyaml", "dev{}dejvokep{}boostedyaml")
    ),
    EXP4J(
            "net{}objecthunter",
            "exp4j",
            "0.4.8",
            "exp4j",
            Relocation.of("exp4j", "net{}objecthunter{}exp4j")
    ),
    MYSQL_DRIVER(
            "com{}mysql",
            "mysql-connector-j",
            "8.4.0",
            "mysql-connector-j",
            Relocation.of("mysql", "com{}mysql")
    ),
    H2_DRIVER(
            "com.h2database",
            "h2",
            "2.2.224",
            "h2database"
    ),
    SQLITE_DRIVER(
            "org.xerial",
            "sqlite-jdbc",
            "3.45.3.0",
            "sqlite-jdbc"
    ),
    HIKARI(
            "com{}zaxxer",
            "HikariCP",
            "5.1.0",
            "HikariCP",
            Relocation.of("hikari", "com{}zaxxer{}hikari")
    ),
    SLF4J_SIMPLE(
            "org.slf4j",
            "slf4j-simple",
            "2.0.12",
            "slf4j-simple"
    ),
    SLF4J_API(
            "org.slf4j",
            "slf4j-api",
            "2.0.12",
            "slf4j-api"
    ),
    MONGODB_DRIVER_CORE(
            "org{}mongodb",
            "mongodb-driver-core",
            "5.1.0",
            "mongodb-driver-core",
            Relocation.of("mongodb", "com{}mongodb"),
            Relocation.of("bson", "org{}bson")
    ),
    MONGODB_DRIVER_SYNC(
            "org{}mongodb",
            "mongodb-driver-sync",
            "5.1.0",
            "mongodb-driver-sync",
            Relocation.of("mongodb", "com{}mongodb"),
            Relocation.of("bson", "org{}bson")
    ),
    MONGODB_DRIVER_BSON(
            "org{}mongodb",
            "bson",
            "5.1.0",
            "mongodb-bson",
            Relocation.of("mongodb", "com{}mongodb"),
            Relocation.of("bson", "org{}bson")
    ),
    JEDIS(
            "redis{}clients",
            "jedis",
            "5.1.2",
            "jedis",
            Relocation.of("jedis", "redis{}clients{}jedis"),
            Relocation.of("commonspool2", "org{}apache{}commons{}pool2")
    ),
    BSTATS_BASE(
            "org{}bstats",
            "bstats-base",
            "3.0.2",
            "bstats-base",
            Relocation.of("bstats", "org{}bstats")
    ),
    BSTATS_BUKKIT(
            "org{}bstats",
            "bstats-bukkit",
            "3.0.2",
            "bstats-bukkit",
            Relocation.of("bstats", "org{}bstats")
    ),
    COMMONS_POOL_2(
            "org{}apache{}commons",
            "commons-pool2",
            "2.12.0",
            "commons-pool2",
            Relocation.of("commonspool2", "org{}apache{}commons{}pool2")
    ),
    GSON(
            "com.google.code.gson",
            "gson",
            "2.10.1",
            "gson"
    ),
    COMMONS_LANG_3(
            "org{}apache{}commons",
            "commons-lang3",
            "3.14.0",
            "commons-lang3",
            Relocation.of("lang3", "org{}apache{}commons{}lang3")
    );

    private final String mavenRepoPath;
    private final String version;
    private final List<Relocation> relocations;
    private final String artifact;

    private static final String MAVEN_FORMAT = "%s/%s/%s/%s-%s.jar";

    Dependency(String groupId, String artifactId, String version, String artifact) {
        this(groupId, artifactId, version, artifact, new Relocation[0]);
    }

    Dependency(String groupId, String artifactId, String version, String artifact, Relocation... relocations) {
        this.mavenRepoPath = String.format(MAVEN_FORMAT,
                rewriteEscaping(groupId).replace(".", "/"),
                rewriteEscaping(artifactId),
                version,
                rewriteEscaping(artifactId),
                version
        );
        this.version = version;
        this.relocations = ImmutableList.copyOf(relocations);
        this.artifact = artifact;
    }

    private static String rewriteEscaping(String s) {
        return s.replace("{}", ".");
    }

    public String getFileName(String classifier) {
        String name = artifact.toLowerCase(Locale.ROOT).replace('_', '-');
        String extra = classifier == null || classifier.isEmpty()
                ? ""
                : "-" + classifier;

        return name + "-" + this.version + extra + ".jar";
    }

    String getMavenRepoPath() {
        return this.mavenRepoPath;
    }

    public List<Relocation> getRelocations() {
        return this.relocations;
    }
}
