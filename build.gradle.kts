plugins {
    id("fabric-loom") version "1.17.20"
    `java-library`
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

repositories {
    maven("https://maven.muon.rip/releases/") { name = "MuonR (Dynamic Difficulty)" }
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    // Provided at runtime by the real Dynamic Difficulty jar already installed
    // on the server - compileOnly, never bundled/included. Transitive deps
    // (modmenu, fzzy_config, mixinsquared) aren't needed to compile against
    // the public API surface (LevelingAPI/PlayerLevelProvider) and aren't on
    // any repository declared here, so they're excluded rather than resolved.
    compileOnly("dev.muon.dynamic_difficulty:dynamic_difficulty-fabric:${project.property("dynamic_difficulty_version")}") {
        isTransitive = false
    }

    // This server's own Apotheosis Fabric port (mod-dev/apotheosis-fabric) isn't
    // published anywhere - compileOnly against the actual installed jar, copied
    // here as a reference jar (not committed, see .gitignore), provided at
    // runtime by the real installed mod.
    compileOnly(files("reference-jars/apotheosis-adventure-fabric-0.1.0.jar"))
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version))
    }
}
