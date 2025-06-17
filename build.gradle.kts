// build.gradle.kts

plugins {
    id("java")
}

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

// ИЗМЕНЕНО: Блок 'tasks' был значительно упрощен.
// Мы убрали лишние инструкции, которые приводили к дублированию plugin.yml.
// Теперь используется стандартное поведение Gradle, которое работает корректно.
tasks.jar {
    // Эта строка остается, чтобы у файла было красивое имя, например, RandomLootChest-1.0.jar
    archiveFileName.set("${rootProject.name}-${project.version}.jar")
}