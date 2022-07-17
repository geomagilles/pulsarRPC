apply(plugin = "java")
apply(plugin = "java-library")
apply(plugin = "maven-publish")
apply(plugin = "signing")

buildscript {
    repositories {
        mavenCentral()
        maven(url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        maven(url = uri("https://plugins.gradle.org/m2/"))
    }
}

repositories {
    mavenCentral()
}

fun Project.publishing(action: PublishingExtension.() -> Unit) = configure(action)

fun Project.signing(configure: SigningExtension.() -> Unit): Unit = configure(configure)

fun Project.java(configure: JavaPluginExtension.() -> Unit): Unit = configure(configure)

val publications: PublicationContainer = (extensions.getByName("publishing") as PublishingExtension).publications

signing {
    if (Ci.isRelease) {
        sign(publications)
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

val ossSonatypeOrgUsername: String? by project
val ossSonatypeOrgPassword: String? by project

publishing {
    repositories {
        val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
        val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven {
            name = "deploy"
            url = if (Ci.isRelease) releasesRepoUrl else snapshotsRepoUrl
            credentials {
                username = System.getenv("OSSRH_USERNAME") ?: ossSonatypeOrgUsername
                password = System.getenv("OSSRH_PASSWORD") ?: ossSonatypeOrgPassword
            }
        }
    }

    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            pom {
                name.set("RPC4P")
                description.set("RPC Framework For Pulsar")
                url.set("https://infinitic.io")

                scm {
                    connection.set("scm:git:https://github.com/infiniticio/rpc4p/")
                    developerConnection.set("scm:git:https://github.com/infiniticio/rpc4p/")
                    url.set("https://github.com/infiniticio/rpc4p/")
                }

                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("geomagilles")
                        name.set("Gilles Barbier")
                        email.set("gilles@infinitic.io")
                    }
                }
            }
        }
    }
}
