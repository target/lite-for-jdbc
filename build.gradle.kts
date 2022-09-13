repositories {
    mavenLocal()
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.7.10"
    `java-library`
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("java-test-fixtures")
}

group = "com.target"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")

    implementation("com.zaxxer:HikariCP:5.0.1")

    implementation("ch.qos.logback:logback-classic:1.4.0")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")
    implementation("org.slf4j:slf4j-api:2.0.0")

    api("com.target:health-monitor-interface:1.2.0")

    testFixturesApi("io.mockk:mockk:1.12.7")

    val junitVersion = "5.9.0"
    val kotestVersion = "5.4.2"
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions:$kotestVersion")
    testImplementation("io.mockk:mockk:1.12.7")

    testApi("com.h2database:h2:2.1.214")
}

tasks {
    compileJava { options.release.set(11) }
    compileKotlin { kotlinOptions { jvmTarget = "11" } }
    compileTestKotlin { kotlinOptions { jvmTarget = "11" } }

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
