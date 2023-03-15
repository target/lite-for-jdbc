repositories {
    mavenLocal()
    mavenCentral()
}

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
    `java-library`
    `maven-publish`
    signing
    alias(libs.plugins.versions)
    alias(libs.plugins.nexusPublishPlugin)
    id("java-test-fixtures")
}

group = "com.target"

dependencies {

    implementation(libs.kotlinReflect)

    implementation(libs.hikariCP)

    implementation(libs.kotlinLogging)

    api(libs.healthMonitorInterface)

    testFixturesApi(libs.mockk)

    testImplementation(libs.bundles.testing)

    testImplementation(libs.bundles.testContainers)

    testImplementation(libs.postgresql)

    testApi(libs.h2)
}

val jvmTargetVersion: String by project

tasks {
    // This line configures the target JVM version. For applications, it's typically the latest LTS version.
    // For libraries, it's typically the earliest LTS version.
    java { toolchain { languageVersion.set(JavaLanguageVersion.of(jvmTargetVersion)) } }

    withType<Test> {
        useJUnitPlatform()

        // this add-opens is to work around a JVM 17 strong encapsulation change. In some tests we're modifying the environment using
        // the `withEnvironment` and `withSystemProperties` kotest system extensions.
        // If you're not using those extensions, you can remove this override.
        jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED")
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            credentials(PasswordCredentials::class)
            name = "sonatype" // correlates with the environment variable set in the github action release.yml publish job
            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            setUrl(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
        }
    }

    publications {
        val projectTitle: String by project
        val projectDescription: String by project
        val projectUrl: String by project
        val projectScm: String by project

        create<MavenPublication>("mavenJava") {
            artifactId = rootProject.name
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set(projectTitle)
                description.set(projectDescription)
                url.set(projectUrl)
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("ossteam")
                        name.set("OSS Office")
                        email.set("ossteam@target.com")
                    }
                }
                scm {
                    url.set(projectScm)
                }
            }
        }
    }

}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    if (signingKey.isNullOrBlank() || signingPassword.isNullOrBlank()) {
        isRequired = false
    } else {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}
