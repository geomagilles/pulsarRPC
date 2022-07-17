import com.google.protobuf.gradle.GenerateProtoTask
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id("com.google.protobuf").version("0.8.18")
}

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.protobuf:protobuf-kotlin:3.20.1")
    implementation("io.grpc:grpc-stub:1.46.0")
    implementation("io.grpc:grpc-protobuf:1.46.0")
    implementation("io.grpc:grpc-core:1.46.0")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("com.salesforce.servicelibs:grpc-contrib:0.8.1")
}

tasks.withType<GenerateProtoTask> {
    dependsOn(":plugin:build", ":plugin:installDist")
}

java.sourceSets["main"].java {
    srcDir("build/generated")
}

protobuf {
    generatedFilesBaseDir = "$projectDir/build/generated"

    protoc {
        artifact = "com.google.protobuf:protoc:3.20.1"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.46.0"
        }
        id("dump") {
            path = "${System.getProperty("user.dir")}/jprotoc/build/install/jprotoc/bin/jprotoc"
        }
        id("plugin") {
            path = "${System.getProperty("user.dir")}/plugin/build/install/plugin/bin/plugin"
        }
    }

    generateProtoTasks {
        all().forEach {
            it.builtins {
                id("kotlin")
            }
            it.plugins {
                id("grpc")
                id("dump")
                id("plugin")
            }
        }
    }
}
