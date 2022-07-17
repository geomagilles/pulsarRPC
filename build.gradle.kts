
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(Plugins.Kotlin.id).version(Plugins.Kotlin.version)
    id(Plugins.Serialization.id).version(Plugins.Serialization.version) apply false
//    id(Plugins.Ktlint.id).version(Plugins.Ktlint.version) apply false
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = Plugins.Kotlin.id)
    apply(plugin = Plugins.Serialization.id)
//    apply(plugin = Plugins.Ktlint.id)

    group = Ci.org
    version = Ci.version

    dependencies {
        implementation(Libs.Logging.api)

        testImplementation(Libs.Slf4j.simple)
        testImplementation(Libs.Kotest.junit5)
        testImplementation(Libs.Kotest.property)
        testImplementation(Libs.Mockk.mockk)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
            jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }

    // Keep this to tell compatibility to applications
    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_11.toString()
        targetCompatibility = JavaVersion.VERSION_11.toString()
    }
}
