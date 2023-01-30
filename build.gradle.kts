repositories {
    mavenLocal()
    mavenCentral()
}

plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin")
    id("java-test-fixtures")
}

group = "com.target"

dependencies {

    val kotlinReflectVersion: String by project
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinReflectVersion")

    val hikariCPVersion: String by project
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")

    val logbackVersion: String by project
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    val kotlinLoggingVersion: String by project
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    val slf4jVersion: String by project
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    val healthMonitorInterfaceVersion: String by project
    api("com.target:health-monitor-interface:$healthMonitorInterfaceVersion")

    val mockkVersion: String by project
    testFixturesApi("io.mockk:mockk:$mockkVersion")

    val junitVersion: String by project
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    val kotestVersion: String by project
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    val testContainersVersion: String by project
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")

    val postgresqlVersion: String by project
    testImplementation("org.postgresql:postgresql:$postgresqlVersion")

    val h2Version: String by project
    testApi("com.h2database:h2:$h2Version")
}

val jvmTargetVersion: String by project
tasks {
    compileJava { options.release.set(jvmTargetVersion.toInt()) }
    compileKotlin { kotlinOptions { jvmTarget = jvmTargetVersion } }
    compileTestKotlin { kotlinOptions { jvmTarget = jvmTargetVersion } }

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
