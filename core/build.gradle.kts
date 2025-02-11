plugins {
    id("io.github.goooler.shadow") version "8.1.8"
    id("maven-publish")
}

repositories {
    maven("https://jitpack.io/")
    maven("https://libraries.minecraft.net/")
}

dependencies {
    implementation(project(":shared"))
    // JOML
    compileOnly("org.joml:joml:1.10.8")
    // YAML
    implementation("dev.dejvokep:boosted-yaml:${rootProject.properties["boosted_yaml_version"]}")
    compileOnly("org.yaml:snakeyaml:${rootProject.properties["snake_yaml_version"]}")
    // NBT
    implementation("com.github.Xiao-MoMi:sparrow-nbt:${rootProject.properties["sparrow_nbt_version"]}")
    // Adventure
    implementation("net.kyori:adventure-api:${rootProject.properties["adventure_bundle_version"]}")
    implementation("net.kyori:adventure-text-minimessage:${rootProject.properties["adventure_bundle_version"]}")
    implementation("net.kyori:adventure-text-serializer-gson:${rootProject.properties["adventure_bundle_version"]}") {
        exclude("com.google.code.gson", "gson")
    }
    // Command
    compileOnly("org.incendo:cloud-core:${rootProject.properties["cloud_core_version"]}")
    compileOnly("org.incendo:cloud-minecraft-extras:${rootProject.properties["cloud_minecraft_extras_version"]}")
    // FastUtil
    compileOnly("it.unimi.dsi:fastutil:${rootProject.properties["fastutil_version"]}")
    // Gson
    compileOnly("com.google.code.gson:gson:${rootProject.properties["gson_version"]}")
    // Guava
    compileOnly("com.google.guava:guava:${rootProject.properties["guava_version"]}")
    // Logger
    compileOnly("org.slf4j:slf4j-api:${rootProject.properties["slf4j_version"]}")
    compileOnly("org.apache.logging.log4j:log4j-core:${rootProject.properties["log4j_version"]}")
    // Netty
    compileOnly("io.netty:netty-all:${rootProject.properties["netty_version"]}.Final")
    // Cache
    compileOnly("com.github.ben-manes.caffeine:caffeine:${rootProject.properties["caffeine_version"]}")
    // Compression
    compileOnly("com.github.luben:zstd-jni:${rootProject.properties["zstd_version"]}")
    compileOnly("org.lz4:lz4-java:${rootProject.properties["lz4_version"]}")
    // Commons IO
    compileOnly("commons-io:commons-io:${rootProject.properties["commons_io_version"]}")
    compileOnly("commons-io:commons-io:${rootProject.properties["commons_io_version"]}")
    // Data Fixer Upper
    compileOnly("com.mojang:datafixerupper:${rootProject.properties["datafixerupper_version"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
    dependsOn(tasks.clean)
}

tasks {
    shadowJar {
        archiveClassifier = ""
        archiveFileName = "craft-engine-${rootProject.properties["project_version"]}.jar"
        relocate("net.kyori", "net.momirealms.craftengine.libraries")
        relocate("dev.dejvokep", "net.momirealms.craftengine.libraries")
        relocate("com.saicone.rtag", "net.momirealms.craftengine.libraries.rtag")
        relocate("org.yaml.snakeyaml", "net.momirealms.craftengine.libraries.snakeyaml")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "net.momirealms"
            artifactId = "craft-engine"
            version = rootProject.version.toString()
            artifact(tasks.shadowJar)
        }
    }
}
