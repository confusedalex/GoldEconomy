import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    kotlin("jvm") version "2.4.20"
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.1.0"
    id("com.modrinth.minotaur") version "2.10.0"
}

group = "dev.confusedalex"
version = "2.1.1"
val targetApiVersion = "1.21.11"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") // MockBukkit and Paper API
    maven("https://repo.codemc.io/repository/creatorfromhell/") // VaultUnlockedAPI
    maven("https://repo.glaremasters.me/repository/towny/") // Towny
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven("https://repo.aikar.co/content/groups/aikar/") // ACF
}

dependencies {
    // Plugins
    compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.20") { isTransitive = false }
    compileOnly("com.palmergames.bukkit.towny:towny:0.101.2.1")
    compileOnly("me.clip:placeholderapi:2.12.3")

    // Internal
    compileOnly("io.papermc.paper:paper-api:${targetApiVersion}-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.2.1")

    // Tests
    // TODO: When updating to the next version of MC, replace "v1.21" with "v${targetApiVersion}" - mockbukkit uploaded 1.21.11 versions under 1.21
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.116.3") {
        // Exclude the JetBrains annotations to prevent conflicts
        exclude(group = "org.jetbrains", module = "annotations")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

runPaper.folia.registerTask {
    version = targetApiVersion
    downloadPlugins {
        url("https://github.com/TheNewEconomy/VaultUnlocked/releases/download/2.20.1/VaultUnlocked-2.20.1.jar")
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
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
        relocationPrefix = "dev.confusedalex.thegoldeconomy.libs"
        exclude("META-INF/**")
        from("LICENSE")
        minimize()
    }

    test {
        useJUnitPlatform()
        systemProperty("bstats.relocatecheck", "false")
    }

    runServer {
        downloadPlugins {
            url("https://github.com/MilkBowl/Vault/releases/download/1.7.3/Vault.jar")
//            url("https://github.com/TheNewEconomy/VaultUnlocked/releases/download/2.20.1/VaultUnlocked-2.20.1.jar")
            modrinth("towny", "0.103.2.0")
        }
        minecraftVersion(targetApiVersion)

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
    versionName.set("TheGoldEconomy $version")
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(
        "1.21.11",
        "26.1",
        "26.1.1",
        "26.1.2",
        "26.2",
        "26.3"
    )
    loaders.addAll("folia", "paper", "purpur")
    syncBodyFrom = rootProject.file("README.md").readText()
}
