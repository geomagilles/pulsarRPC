package io.pulsarRPC.jprotoc

import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.GeneratedMessage
import com.google.protobuf.compiler.PluginProtos
import java.io.File
import java.io.IOException

/**
 * This method is designed to be used by unit tests. Unit testing typically works like this:
 * 1. Add the Dump protoc plugin to your test code generation build phase, to write out a proto dump file.
 * 2. Locate the generated dump file in a unit test.
 * 3. Call this method, with the path of the dump file from a test.
 * 4. Inspect the generated output.
 */
object ProtocPluginTesting {
    const val MAVEN_DUMP_PATH = "target/generated-test-sources/protobuf/dump/descriptor_dump"

    /**
     * Debug a single generator using the parsed proto descriptor.
     * @param generator The generator to run.
     * @param dumpPath The path to a descriptor dump on the filesystem.
     * @return The compiled output from the protoc plugin
     */
    @Throws(IOException::class)
    fun test(
        generator: Generator,
        dumpPath: String
    ): PluginProtos.CodeGeneratorResponse = test(listOf(generator), dumpPath)

    /**
     * Debug multiple generators using the parsed proto descriptor, aggregating their results.
     * @param generators The list of generators to run.
     * @param dumpPath The path to a descriptor dump on the filesystem.
     * @return The compiled output from the protoc plugin
     */
    @Throws(IOException::class)
    fun test(
        generators: List<Generator>,
        dumpPath: String
    ): PluginProtos.CodeGeneratorResponse = test(generators, emptyList(), dumpPath)

    /**
     * Test multiple generators using the parsed proto descriptor, aggregating their results.
     * Also register the given extensions so they may be processed by the generator.
     *
     * @param generators The list of generators to run.
     * @param extensions The list of extensions to register.
     * @param dumpPath The path to a descriptor dump on the filesystem.
     * @return The compiled output from the protoc plugin
     */
    @Throws(IOException::class)
    fun test(
        generators: List<Generator>,
        extensions: List<GeneratedMessage.GeneratedExtension<*, *>>,
        dumpPath: String
    ): PluginProtos.CodeGeneratorResponse {
        require(generators.isNotEmpty()) { "generators.isEmpty()" }
        require(extensions.isNotEmpty()) { "extensions.isEmpty()" }

        // As per https://developers.google.com/protocol-buffers/docs/reference/java-generated#extension,
        // extensions must be registered in order to be processed.
        val extensionRegistry = ExtensionRegistry.newInstance()
        for (extension in extensions) {
            extensionRegistry.add(extension)
        }
        val generatorRequestBytes: ByteArray = File(dumpPath).readBytes()
        val request = PluginProtos.CodeGeneratorRequest.parseFrom(generatorRequestBytes, extensionRegistry)

        return ProtocPlugin.generate(generators, request)
    }
}
