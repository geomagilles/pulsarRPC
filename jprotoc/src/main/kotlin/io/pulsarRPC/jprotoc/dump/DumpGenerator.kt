package io.pulsarRPC.jprotoc.dump

import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.compiler.PluginProtos
import io.pulsarRPC.jprotoc.Generator
import io.pulsarRPC.jprotoc.GeneratorException

/**
 * Dumps the content of the input descriptor set to descriptor_dump.json.
 */
class DumpGenerator : Generator() {

    override fun supportedFeatures(): List<PluginProtos.CodeGeneratorResponse.Feature> {
        return listOf(PluginProtos.CodeGeneratorResponse.Feature.FEATURE_PROTO3_OPTIONAL)
    }

    @Throws(GeneratorException::class)
    override fun generateFiles(request: PluginProtos.CodeGeneratorRequest) = try {
        listOf(
            makeFile("descriptor_dump", request.toByteArray()),
            makeFile("descriptor_dump.yml", request.toString())
        )
    } catch (e: InvalidProtocolBufferException) {
        throw GeneratorException(e.message)
    }
}
