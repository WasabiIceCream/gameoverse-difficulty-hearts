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
    // The installed 1.3.3 jar rather than the maven artifact (1.3.2): BiomeBonusSwitchMixin needs
    // DimensionLevelingSettingsStore, which 1.3.2 doesn't have. Same reference-jar arrangement as below.
    compileOnly(files("reference-jars/dynamic_difficulty-fabric-1.3.3+26.1.2.jar"))

    // This server's own Apotheosis Fabric port (mod-dev/apotheosis-fabric) isn't
    // published anywhere - compileOnly against the actual installed jar, copied
    // here as a reference jar (not committed, see .gitignore), provided at
    // runtime by the real installed mod.
    compileOnly(files("reference-jars/apotheosis-adventure-fabric-0.3.0.jar"))

    // fzzy_config (Dynamic Difficulty's config library), for reading its passive-mob toggle in
    // PassiveMobLevelsMixin. Same reference-jar arrangement as above.
    compileOnly(files("reference-jars/fzzy_config-0.7.6+26.1.jar"))
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
