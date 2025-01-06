plugins {
    id("java")
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "dev.confusedalex"
version = "1.8.1"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.glaremasters.me/repository/towny/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")

    compileOnly("org.spigotmc:spigot-api:1.18-rc3-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("com.palmergames.bukkit.towny:towny:0.98.1.0")
    compileOnly("me.clip:placeholderapi:2.11.6")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.1.0")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
    withJavadocJar()
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    compileTestJava {
        options.encoding = "UTF-8"
    }

    // Disable the default JAR task
    jar {
        enabled = false
    }

    javadoc {
        options.encoding = "UTF-8"
    }

    shadowJar {
        relocate("co.aikar.commands", "confusedalex.thegoldeconomy.acf")
        relocate("co.aikar.locales", "confusedalex.thegoldeconomy.locales")
        archiveClassifier.set("")
    }
}