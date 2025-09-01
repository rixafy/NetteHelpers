import kotlinx.kover.tasks.KoverXmlTask
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.7.2"
    id("org.jetbrains.changelog") version "2.2.0"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    intellijPlatform {
        val type = providers.gradleProperty("platformType")
        val version = providers.gradleProperty("platformVersion")

        create(type, version)

        val platformPlugins = properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
        plugins(platformPlugins)
    }
}

// https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html#intellijPlatform
intellijPlatform {
    pluginConfiguration {
        name.set(properties("pluginName"))
        version.set(properties("pluginVersion"))
        ideaVersion {
            sinceBuild.set(properties("pluginSinceBuild"))
        }
        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description.set(providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        })
        changeNotes.set(provider {
            changelog.getAll().values.joinToString("\n") { changelog.renderItem(it, Changelog.OutputType.HTML) }
        })
    }

    autoReload = true
}

// https://github.com/Kotlin/kotlinx-kover#configuration
kover.xmlReport {
    onCheck.set(true)
}

changelog {
    version.set(properties("pluginVersion"))
    repositoryUrl = properties("pluginRepositoryUrl")
    path.set(file("CHANGELOG.md").canonicalPath)
}

tasks {
    // Set the JVM compatibility versions
    properties("javaVersion").get().let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
    }

    withType<KoverXmlTask> {
        dependsOn("compileJava")
    }

    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }


    publishPlugin {
        token.set(environment("PLUGIN_PUBLISH_TOKEN"))
    }
}
