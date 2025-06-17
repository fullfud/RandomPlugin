plugins {
    id("java")
}

group = "com.fullfud.randomlootchest"
version = "1.0-FINAL"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
}

tasks.jar {
    archiveFileName.set("${rootProject.name}-${project.version}.jar")
}