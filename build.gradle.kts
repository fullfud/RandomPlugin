plugins {
    id("java")
}

// ИЗМЕНЕНО: group теперь com.fullfud.randomlootchest
group = "com.fullfud.randomlootchest"
version = "1.0"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
}

tasks {
    build {
        dependsOn(jar)
    }

    jar {
        archiveFileName.set("${rootProject.name}-${project.version}.jar")
        from("src/main/resources") {
            include("plugin.yml")
        }
    }
}