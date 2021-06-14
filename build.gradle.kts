@file:Suppress("PropertyName")

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options

plugins {
    java
    idea
    `maven-publish`
    id("fabric-loom") version "0.8-SNAPSHOT"
    id("com.github.ben-manes.versions") version "0.39.0"
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("com.diffplug.spotless") version "5.12.5"
    kotlin("jvm") version "1.4.21"
    id("org.shipkit.shipkit-auto-version") version "1.+"
    id("org.shipkit.shipkit-changelog") version "1.+"
    id("org.shipkit.shipkit-github-release") version "1.+"
}

tasks.withType<DependencyUpdatesTask> {
    gradleReleaseChannel = "current"
    rejectVersionIf {
        candidate.version.contains("""-M\d+""".toRegex())
                || candidate.version.contains("RC")
    }
}

repositories {
    maven {
        url = uri("https://maven.fabricmc.net/")
        name = "Fabric"
    }
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

val archives_base_name: String by project
val maven_group: String by project
val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val fabric_version: String by project
val curseforge_id: String by project

base {
    archivesBaseName = archives_base_name
    group = maven_group
}

minecraft {
}

dependencies {
    //to change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings("net.fabricmc:yarn:${yarn_mappings}:v2")
    modImplementation("net.fabricmc:fabric-loader:${loader_version}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_version}")
}

tasks.processResources {
    inputs.properties(
        "version" to project.version
    )

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version
        )
    }
}

tasks.withType<JavaCompile> {
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    // If Javadoc is generated, this must be specified in that task too.
    options.encoding = "UTF-8"
    // Minecraft 1.17 (21w19a) upwards uses Java 16.
    options.release.set(16)
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mod") {
            artifact(tasks.remapJar)
            artifact(tasks["sourcesJar"]) {
                builtBy(tasks.remapSourcesJar)
            }
        }
    }

    repositories {
        mavenLocal()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/magneticflux-/fabric-tree-chopper")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

tasks.generateChangelog {
    repository = "magneticflux-/fabric-tree-chopper"
    previousRevision = project.ext["shipkit-auto-version.previous-tag"]?.toString()
    githubToken = System.getenv("GITHUB_TOKEN")
}

tasks.githubRelease {
    dependsOn(tasks.generateChangelog)
    repository = "magneticflux-/fabric-diagonal-panes"
    changelog = tasks.generateChangelog.get().outputFile
    githubToken = System.getenv("GITHUB_TOKEN")
    newTagRevision = System.getenv("GITHUB_SHA")
}

// Configure CurseForge publishing
curseforge {
    // Stored in ~/.gradle/gradle.properties
    when {
        project.hasProperty("curseApiKey") -> apiKey = project.ext["curseApiKey"]
        System.getenv("CURSE_API_KEY") != null -> apiKey = System.getenv("CURSE_API_KEY")
        else -> println("No CurseForge API key found, \'curseforge\' tasks will not work")
    }

    project(closureOf<CurseProject> {
        id = curseforge_id
        releaseType = "release"
        addGameVersion(minecraft_version)
        addGameVersion("Fabric")
        changelog =
            "View the latest changelog here: https://github.com/magneticflux-/fabric-tree-chopper/releases"
        mainArtifact(tasks.remapJar.get(), closureOf<CurseArtifact> {
            relations(closureOf<CurseRelation> {
                requiredDependency("fabric-api")
            })
        })
        addArtifact(tasks["sourcesJar"])
    })
    options(closureOf<Options> {
        forgeGradleIntegration = false
    })
}

spotless {
}


afterEvaluate {
    // CurseGradle generates tasks in afterEvaluate for each project
    // There isn't really any other way to make it depend on a task unless it is an AbstractArchiveTask
    val curseforgeTask = tasks.getByName("curseforge${curseforge_id}")
    tasks.publish {
        dependsOn(curseforgeTask)
    }
}
