import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id("com.google.protobuf").version("0.8.18")

    application
}

application {
    mainClass.set("io.pulsarRPC.jprotoc.dump.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.protobuf:protobuf-kotlin:3.20.1")
    api("com.github.spullara.mustache.java:compiler:0.9.10")
}

protobuf {

    protoc {
        artifact = "com.google.protobuf:protoc:3.20.1"
    }

    generateProtoTasks {
        all().forEach {
            it.builtins {
                id("kotlin")
            }
            it.plugins {
                id("jprotoc")
            }
        }
    }
}

apply("../publish.gradle.kts")
