@file:Suppress("UnstableApiUsage")

import net.fabricmc.loom.api.LoomGradleExtensionAPI

val license: String by project
val minecraftVersion: String by project
val modVersion: String by project
val modPackage: String by project
val modId: String by project
val modName: String by project
val modAuthor: String by project
val modDescription: String by project
val parchmentVersion: String by project
val forgeVersion: String by project
val kjsVersion: String by project
val eioVersion: String by project
val jeiVersion: String by project
val githubUser: String by project
val githubRepo: String by project

plugins {
    id("dev.architectury.loom") version "1.2.+" // TODO: update to 1.3.+ when JiJ fix is forward ported
    id("io.github.juuxel.loom-vineflower") version "1.11.0"
    id("com.github.gmazzo.buildconfig") version "4.0.4"
    java
    `maven-publish`
}

base {
    version = "$minecraftVersion-$modVersion"
    archivesName.set("$modId-forge")
}

loom {
    silentMojangMappingsLicense()

    forge {
        mixinConfig("$modId.mixins.json")
    }

    if (project.findProperty("enableAccessWidener") == "true") {
        accessWidenerPath.set(file("src/main/resources/$modId.accesswidener"))
        forge {
            convertAccessWideners.set(true)
            extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
        }
        println("Access widener enabled for project. Access widener path: ${loom.accessWidenerPath.get()}")
    }
}

repositories {
    maven("https://maven.parchmentmc.org") // Parchment
    maven("https://maven.latvian.dev/releases") // KubeJS
    maven("https://maven.rover656.dev/releases") // EnderIO
    maven("https://maven.tterrag.com") // Registrate for EnderIO
    maven ( "https://cursemaven.com" ) // Cursemaven
    maven("http://dogforce-games.com/maven") {

        isAllowInsecureProtocol = true
    } // GraphLib for EnderIO
    maven("https://maven.blamejared.com") // JEI
    mavenLocal()
}

dependencies {
    // Minecraft
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$minecraftVersion:$parchmentVersion@zip")
    })

    // Forge
    forge("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")

    // KubeJS
    modImplementation("dev.latvian.mods:kubejs-forge:$kjsVersion")
    localRuntime("io.github.llamalad7:mixinextras-forge:0.2.0-rc.4")

    // EnderIO
    modImplementation("com.enderio:EnderIO:$minecraftVersion-$eioVersion")
    modImplementation("dev.gigaherz.graph:GraphLib3:3.0.4")
    modImplementation("com.tterrag.registrate:Registrate:MC1.20-1.3.11")

    // JEI
    modCompileOnly("mezz.jei:jei-$minecraftVersion-common-api:$jeiVersion") { isTransitive = false }
    modLocalRuntime("mezz.jei:jei-$minecraftVersion-forge:$jeiVersion") { isTransitive = false }
}

tasks {
    processResources {
        val resourceTargets = listOf("META-INF/mods.toml", "pack.mcmeta")

        val replaceProperties = mapOf(
            "license" to license,
            "minecraftVersion" to minecraftVersion,
            "version" to project.version as String,
            "modId" to modId,
            "modName" to modName,
            "modAuthor" to modAuthor,
            "modDescription" to modDescription,
            "forgeVersion" to forgeVersion,
            // use major version for FML only because wrong Forge version error message
            // is way better than FML error message
            "forgeFMLVersion" to forgeVersion.substringBefore("."),
            "kjsVersion" to kjsVersion,
            "eioVersion" to eioVersion,
            "githubUser" to githubUser,
            "githubRepo" to githubRepo
        )

        println("[Process Resources] Replacing resource properties: ")
        replaceProperties.forEach { (key, value) -> println("\t -> $key = $value") }

        inputs.properties(replaceProperties)
        filesMatching(resourceTargets) {
            expand(replaceProperties)
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    withType<GenerateModuleMetadata> {
        enabled = false
    }
}

extensions.configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

extensions.configure<LoomGradleExtensionAPI> {
    runs {
        forEach {
            val dir = "run/${it.environment}"
            println("[Run Config] '${it.name}' directory: $dir")
            it.runDir(dir)
            // allows DCEVM hot-swapping when using the JBR (https://github.com/JetBrains/JetBrainsRuntime)
            it.vmArgs("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
        }
    }
}

buildConfig {
    buildConfigField("String", "MOD_ID", "\"$modId\"")
    buildConfigField("String", "MOD_NAME", "\"$modName\"")
    buildConfigField("String", "MOD_VERSION", "\"$version\"")
    packageName(modPackage)
    className("KubeIOConstants")
    useJavaOutput()
}
