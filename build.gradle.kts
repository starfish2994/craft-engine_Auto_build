plugins {
    id("java")
}

val git: String = versionBanner()
val builder: String = builder()
ext["git_version"] = git
ext["builder"] = builder

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    repositories {
        mavenCentral()
        maven("https://jitpack.io/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    tasks.processResources {
        filteringCharset = "UTF-8"

        filesMatching(arrayListOf("craft-engine.properties")) {
            expand(rootProject.properties)
        }

        filesMatching(arrayListOf("commands.yml", "config.yml")) {
            expand(
                Pair("project_version", rootProject.properties["project_version"]),
                Pair("config_version", rootProject.properties["config_version"]),
                Pair("lang_version", rootProject.properties["lang_version"])
            )
        }
    }
}

/**
 * 获取 git 提交短哈希，兼容 CI 环境
 */
fun versionBanner(): String {
    return try {
        // 尝试从环境变量获取（GitHub Actions 中可通过 GITHUB_SHA 获得）
        val envSha = System.getenv("GITHUB_SHA")
        if (envSha != null && envSha.length >= 8) {
            return envSha.substring(0, 8)
        }
        // 环境变量获取失败时，再尝试 git 命令
        project.providers.exec {
            commandLine("git", "rev-parse", "--short=8", "HEAD")
            isIgnoreExitValue = true // 忽略命令退出码，避免构建中断
        }.standardOutput.asText.map { it.trim() }.getOrElse("Unknown")
    } catch (e: Exception) {
        "Unknown"
    }
}

/**
 * 获取构建者信息，兼容 CI 环境
 */
fun builder(): String {
    return try {
        // 尝试从环境变量获取（GitHub Actions 中默认为 github-actions[bot]）
        val envBuilder = System.getenv("GITHUB_ACTOR") ?: System.getenv("USER")
        if (envBuilder != null) {
            return envBuilder
        }
        // 环境变量获取失败时，再尝试 git 命令
        project.providers.exec {
            commandLine("git", "config", "user.name")
            isIgnoreExitValue = true // 忽略命令退出码，避免构建中断
        }.standardOutput.asText.map { it.trim() }.getOrElse("Unknown")
    } catch (e: Exception) {
        "Unknown"
    }
}
