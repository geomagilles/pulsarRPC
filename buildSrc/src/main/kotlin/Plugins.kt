const val kotlinVersion = "1.6.21"

object Plugins {
    object Kotlin {
        const val id = "org.jetbrains.kotlin.jvm"
        const val version = kotlinVersion
    }

    object Serialization {
        const val id = "org.jetbrains.kotlin.plugin.serialization"
        const val version = kotlinVersion
    }

    object Ktlint {
        const val id = "org.jlleitschuh.gradle.ktlint"
        const val version = "10.2.1"
    }
}
