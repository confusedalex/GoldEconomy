import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    kotlin("jvm") version "2.4.10"
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.modrinth.minotaur") version "2.+"
}

group = "dev.confusedalex"
version = "1.13.0"
val targetApiVersion = "1.21.11"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") // MockBukkit and Paper API
    maven("https://repo.codemc.io/repository/creatorfromhell/") // VaultUnlockedAPI
    maven("https://repo.glaremasters.me/repository/towny/") // Towny
    maven("https://oss.sonatype.org/content/groups/public/")
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.4.10")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.apache.commons:commons-lang3:3.20.0")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")

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
    withSourcesJar()
    withJavadocJar()
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
            url("https://github.com/TheNewEconomy/VaultUnlocked/releases/download/2.20.1/VaultUnlocked-2.20.1.jar")
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
        "1.21.10",
        "1.21.11"
    )
    loaders.addAll("spigot", "paper", "purpur")
    syncBodyFrom = rootProject.file("README.md").readText()
}
