group = "io.pulsarRPC"
version = "0.0.1"

plugins {
    application
}

application {
    mainClass.set("io.pulsarRPC.plugin.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.salesforce.servicelibs:jprotoc:1.2.1")
//    api(project(":jprotoc"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
