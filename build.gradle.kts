plugins {
    kotlin("jvm") version "1.7.20"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "net.byteslayer"
version = "1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    implementation(kotlin("stdlib"))
}

tasks {
    shadowJar {
        archiveFileName.set("NightspaceFly-${project.version}.jar")
        relocate("kotlin", "net.byteslayer.nightspacefly.kotlin")
    }
    build {
        dependsOn(shadowJar)
    }
}