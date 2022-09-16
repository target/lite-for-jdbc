rootProject.name = "lite-for-jdbc"

pluginManagement {
    val kotlinPluginVersion: String by settings
    val nexusPublishPluginVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinPluginVersion
        id("io.github.gradle-nexus.publish-plugin") version nexusPublishPluginVersion
    }
}
