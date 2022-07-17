object Libs {

    object Kotlin {
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"
    }

    object Coroutines {
        private const val version = "1.6.1"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val jdk8 = "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$version"
    }

    object Serialization {
        const val json = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2"
    }

    object Jackson {
        private const val version = "2.13.2"
        const val core = "com.fasterxml.jackson.core:jackson-core:$version"
        const val databind = "com.fasterxml.jackson.core:jackson-databind:$version"
        const val kotlin = "com.fasterxml.jackson.module:jackson-module-kotlin:$version"
        const val jsr310 = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$version"
    }

    object Kotest {
        private const val version = "5.2.3"
        const val property = "io.kotest:kotest-property-jvm:$version"
        const val junit5 = "io.kotest:kotest-runner-junit5-jvm:$version"
    }

    object Mockk {
        const val mockk = "io.mockk:mockk:1.12.3"
    }

    object Pulsar {
        private const val version = "2.10.0"
        const val client = "org.apache.pulsar:pulsar-client:$version"
        const val clientAdmin = "org.apache.pulsar:pulsar-client-admin:$version"
    }

    object Slf4j {
        private const val version = "1.7.36"
        const val simple = "org.slf4j:slf4j-simple:$version"
        const val api = "org.slf4j:slf4j-api:$version"
    }

    object Logging {
        const val api = "io.github.microutils:kotlin-logging:2.1.21"
    }
}
