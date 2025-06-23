import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "9.0.0-beta17"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "dev.confusedalex"
version = "1.9.0"

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
    compileOnly("com.palmergames.bukkit.towny:towny:0.101.1.9")
    compileOnly("me.clip:placeholderapi:2.11.6")

    // Internal
    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.21")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")

    // Tests
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.50.0") {
        // Exclude the JetBrains annotations to prevent conflicts
        exclude(group = "org.jetbrains", module = "annotations")
    }
    testImplementation("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(platform("org.junit:junit-bom:5.13.1"))
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
        enableRelocation = true
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
        minecraftVersion("1.21.4")
    }
}

configurations {
    configurations.testImplementation.get().apply {
        extendsFrom(configurations.compileOnly.get())
        exclude("org.spigotmc", "spigot-api")
    }
}
