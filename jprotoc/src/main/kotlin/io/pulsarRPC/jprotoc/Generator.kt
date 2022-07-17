/*
 *  Copyright (c) 2019, Salesforce.com, Inc.
 *  All rights reserved.
 *  Licensed under the BSD 3-Clause license.
 *  For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package io.pulsarRPC.jprotoc

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import com.github.mustachejava.MustacheFactory
import com.google.protobuf.ByteString
import com.google.protobuf.compiler.PluginProtos
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringWriter

/**
 * Generator is the base class for all protoc generators managed by [ProtocPlugin].
 */
abstract class Generator {

    /**
     * Processes a generator request into a set of files to output.
     *
     * @param request The raw generator request from protoc.
     * @return The completed files to write out.
     */
    @Throws(GeneratorException::class)
    open fun generateFiles(request: PluginProtos.CodeGeneratorRequest): List<PluginProtos.CodeGeneratorResponse.File> {
        return emptyList()
    }

    /**
     * Signals to protoc which additional generator features this Generator supports. By default, this method returns
     * FEATURE_NONE. You must override this method and supply a value, like FEATURE_PROTO3_OPTIONAL.
     *
     * @return A list of enumerated features.
     */
    open fun supportedFeatures(): List<PluginProtos.CodeGeneratorResponse.Feature> {
        return listOf(PluginProtos.CodeGeneratorResponse.Feature.FEATURE_NONE)
    }

    /**
     * Executes a mustache template against a generatorContext object to generate an output string.
     * @param resourcePath Embedded resource template to use.
     * @param generatorContext Context object to bind the template to.
     * @return The string that results.
     */
    protected fun applyTemplate(
        resourcePath: String,
        generatorContext: Any
    ): String {
        val resource: InputStream = MustacheFactory::class.java.classLoader.getResourceAsStream(resourcePath)
            ?: throw RuntimeException("Could not find resource $resourcePath")
        val resourceReader = InputStreamReader(resource, Charsets.UTF_8)
        val template: Mustache = MUSTACHE_FACTORY.compile(resourceReader, resourcePath)

        return template.execute(StringWriter(), generatorContext).toString()
    }

    /**
     * Creates a protobuf file message from a given name and content.
     * @param fileName The name of the file to generate.
     * @param fileContent The content of the generated file.
     * @return The protobuf file.
     */
    protected fun makeFile(fileName: String, fileContent: String): PluginProtos.CodeGeneratorResponse.File {
        return PluginProtos.CodeGeneratorResponse.File
            .newBuilder()
            .setName(fileName)
            .setContent(fileContent)
            .build()
    }

    /**
     * Creates a protobuf file message from a given name and content.
     * @param fileName The name of the file to generate.
     * @param fileContent The content of the generated file.
     * @return The protobuf file.
     */
    protected fun makeFile(fileName: String, fileContent: ByteArray) = PluginProtos.CodeGeneratorResponse.File
        .newBuilder()
        .setName(fileName)
        .setContentBytes(ByteString.copyFrom(fileContent))
        .build()!!

    companion object {
        private val MUSTACHE_FACTORY: MustacheFactory = DefaultMustacheFactory()
    }
}
