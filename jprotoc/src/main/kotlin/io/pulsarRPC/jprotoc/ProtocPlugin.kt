package io.pulsarRPC.jprotoc

import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.GeneratedMessage
import com.google.protobuf.compiler.PluginProtos
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.system.exitProcess

/**
 * ProtocPlugin is the main entry point for running one or more java-base protoc plugins. This class handles
 * I/O marshaling and error reporting.
 */
object ProtocPlugin {
    /**
     * Apply a single generator to the parsed proto descriptor.
     * @param generator The generator to run.
     */
    fun generate(generator: Generator) {
        generate(listOf(generator))
    }

    /**
     * Apply multiple generators to the parsed proto descriptor, aggregating their results.
     * @param generators The list of generators to run.
     */
    fun generate(generators: List<Generator>) {
        generate(generators, emptyList())
    }

    /**
     * Apply multiple generators to the parsed proto descriptor, aggregating their results.
     * Also register the given extensions so they may be processed by the generator.
     *
     * @param generators The list of generators to run.
     * @param extensions The list of extensions to register.
     */
    fun generate(
        generators: List<Generator>,
        extensions: List<GeneratedMessage.GeneratedExtension<*, *>>
    ) {
        require(generators.isNotEmpty()) { "generators should not be empty" }

        // As per https://developers.google.com/protocol-buffers/docs/reference/java-generated#extension,
        // extensions must be registered in order to be processed.
        val extensionRegistry = ExtensionRegistry.newInstance()
        for (extension in extensions) {
            extensionRegistry.add(extension)
        }
        try {
            // Parse the input stream to extract the generator request
            val request = PluginProtos.CodeGeneratorRequest.parseFrom(System.`in`.readBytes(), extensionRegistry)
            val response = generate(generators, request)
            response.writeTo(System.out)
        } catch (ex: GeneratorException) {
            try {
                PluginProtos.CodeGeneratorResponse
                    .newBuilder()
                    .setError(ex.message)
                    .build()
                    .writeTo(System.out)
            } catch (ex2: IOException) {
                abort(ex2)
            }
        } catch (ex: Throwable) { // Catch all the things!
            abort(ex)
        }
    }

    /**
     * Debug a single generator using the parsed proto descriptor.
     * @param generator The generator to run.
     * @param dumpPath The path to a descriptor dump on the filesystem.
     */
    fun debug(generator: Generator, dumpPath: String) {
        debug(listOf(generator), dumpPath)
    }

    /**
     * Debug multiple generators using the parsed proto descriptor, aggregating their results.
     * @param generators The list of generators to run.
     * @param dumpPath The path to a descriptor dump on the filesystem.
     */
    fun debug(generators: List<Generator>, dumpPath: String) {
        debug(generators, emptyList(), dumpPath)
    }

    /**
     * Debug multiple generators using the parsed proto descriptor, aggregating their results.
     * Also register the given extensions so they may be processed by the generator.
     *
     * @param generators The list of generators to run.
     * @param extensions The list of extensions to register.
     * @param dumpPath The path to a descriptor dump on the filesystem.
     */
    fun debug(
        generators: List<Generator>,
        extensions: List<GeneratedMessage.GeneratedExtension<*, *>>,
        dumpPath: String
    ) {
        require(generators.isNotEmpty()) { "generators should not be empty" }
        require(extensions.isNotEmpty()) { "extensions should not be empty" }

        // As per https://developers.google.com/protocol-buffers/docs/reference/java-generated#extension,
        // extensions must be registered in order to be processed.
        val extensionRegistry = ExtensionRegistry.newInstance()
        for (extension in extensions) {
            extensionRegistry.add(extension)
        }
        try {
            val request = PluginProtos.CodeGeneratorRequest.parseFrom(File(dumpPath).readBytes(), extensionRegistry)
            val response = generate(generators, request)

            // Print error if present
            if (!response.error.isNullOrEmpty()) { System.err.println(response.error) }

            // Write files if present
            for (file in response.fileList) {
                val outFile = if (file.insertionPoint.isNullOrEmpty()) {
                    File(file.name)
                } else {
                    // Append insertion point to file name
                    File(with(File(file.name)) { "$nameWithoutExtension-${file.insertionPoint}$extension" })
                }
                Files.createDirectories(outFile.parentFile.toPath())
                outFile.writeText(file.content, Charsets.UTF_8)
                outFile.writeBytes(file.contentBytes.toByteArray())
            }
        } catch (ex: Throwable) {
            // Catch all the things!
            ex.printStackTrace()
        }
    }

    fun generate(
        generators: List<Generator>,
        request: PluginProtos.CodeGeneratorRequest
    ): PluginProtos.CodeGeneratorResponse {
        require(generators.isNotEmpty()) { "generators should not be empty" }

        // Run each file generator, collecting the output
        val files = generators.flatMap { it.generateFiles(request) }
        val featureMask: Int = generators
            .stream()
            .map { it.supportedFeatures().stream() }
            // OR each generator's feature set together into a mask
            .map { stream ->
                stream
                    .map { it.number }
                    .reduce { l, r -> l or r }
                    .orElse(PluginProtos.CodeGeneratorResponse.Feature.FEATURE_NONE_VALUE)
            }
            // AND together all the masks
            .reduce { l, r -> l and r }
            .orElse(PluginProtos.CodeGeneratorResponse.Feature.FEATURE_NONE_VALUE)

        // Send the files back to protoc
        return PluginProtos.CodeGeneratorResponse
            .newBuilder()
            .addAllFile(files)
            .setSupportedFeatures(featureMask.toLong())
            .build()
    }

    private fun abort(ex: Throwable) {
        ex.printStackTrace(System.err)
        exitProcess(1)
    }
}
