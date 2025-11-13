import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    kotlin("jvm") version "2.2.21"
    id("com.gradleup.shadow") version "9.2.2"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.modrinth.minotaur") version "2.+"
}

group = "dev.confusedalex"
version = "1.9.5"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // SpigotAPI
    maven("https://jitpack.io") // VaultAPI
    maven("https://repo.glaremasters.me/repository/towny/") // Towny
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven("https://repo.aikar.co/content/groups/aikar/") // ACF
    maven("https://repo.papermc.io/repository/maven-public/") // MockBukkit
}

dependencies {
    // Plugins
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1") { isTransitive = false }
    compileOnly("com.palmergames.bukkit.towny:towny:0.101.2.1")
    compileOnly("me.clip:placeholderapi:2.11.7")

    // Internal
    compileOnly("org.spigotmc:spigot-api:1.21.8-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.2.21")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.apache.commons:commons-lang3:3.19.0")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")

    // Tests
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.77.0") {
        // Exclude the JetBrains annotations to prevent conflicts
        exclude(group = "org.jetbrains", module = "annotations")
    }
    testImplementation("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(platform("org.junit:junit-bom:5.14.1"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
    withJavadocJar()
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }

    compileTestJava {
        options.encoding = "UTF-8"
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    compileTestKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }

    // Disable the default JAR task
    jar {
        enabled = false
    }

    javadoc {
        options.encoding = "UTF-8"
    }

    shadowJar {
        archiveClassifier.set("")
        enableAutoRelocation = true
        relocationPrefix = "confusedalex.thegoldeconomy.libs"
        exclude("META-INF/**")
        from("LICENSE")
        minimize()
    }

    test {
        useJUnitPlatform()
    }

    runServer {
        downloadPlugins {
            url("https://github.com/MilkBowl/Vault/releases/download/1.7.3/Vault.jar")
        }
        minecraftVersion("1.21.8")
    }
}

configurations {
    configurations.testImplementation.get().apply {
        extendsFrom(configurations.compileOnly.get())
        exclude("org.spigotmc", "spigot-api")
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("thegoldeconomy")
    versionType.set("release")
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(
        "1.18",
        "1.18.1",
        "1.18.2",
        "1.19",
        "1.19.1",
        "1.19.2",
        "1.19.3",
        "1.19.4",
        "1.20",
        "1.20.1",
        "1.20.2",
        "1.20.3",
        "1.20.4",
        "1.20.5",
        "1.20.6",
        "1.21",
        "1.21.1",
        "1.21.2",
        "1.21.3",
        "1.21.4",
        "1.21.5",
        "1.21.6",
        "1.21.7",
        "1.21.8",
        "1.21.9",
        "1.21.10"
    )
    loaders.addAll("spigot", "paper", "purpur")
    syncBodyFrom = rootProject.file("README.md").readText()
}
