package io.pulsarRPC.jprotoc.jdk8

import com.google.protobuf.DescriptorProtos
import com.google.protobuf.compiler.PluginProtos
import io.pulsarRPC.jprotoc.Generator
import io.pulsarRPC.jprotoc.GeneratorException
import io.pulsarRPC.jprotoc.ProtoTypeMap

/**
 * Generates a set of gRPC stubs that support JDK8 [java.util.concurrent.CompletableFuture].
 */
class Jdk8Generator : Generator() {
    override fun supportedFeatures(): List<PluginProtos.CodeGeneratorResponse.Feature> {
        return listOf(PluginProtos.CodeGeneratorResponse.Feature.FEATURE_PROTO3_OPTIONAL)
    }

    @Throws(GeneratorException::class)
    override fun generateFiles(request: PluginProtos.CodeGeneratorRequest): List<PluginProtos.CodeGeneratorResponse.File> {
        val protoTypeMap: ProtoTypeMap = ProtoTypeMap.of(request.protoFileList)
        val files: MutableList<PluginProtos.CodeGeneratorResponse.File> = ArrayList()
        for (protoFile in request.protoFileList) {
            if (request.fileToGenerateList.contains(protoFile.name)) {
                for (ctx in extractContext(protoTypeMap, protoFile)) {
                    files.add(buildFile(ctx))
                }
            }
        }
        return files
    }

    private fun extractContext(
        protoTypeMap: ProtoTypeMap,
        fileProto: DescriptorProtos.FileDescriptorProto
    ): List<ServiceContext> {
        val serviceContexts: MutableList<ServiceContext> = ArrayList()
        val locations = fileProto.sourceCodeInfo.locationList
        locations.stream()
            .filter { location: DescriptorProtos.SourceCodeInfo.Location ->
                location.pathCount == 2 && location.getPath(
                    0
                ) == DescriptorProtos.FileDescriptorProto.SERVICE_FIELD_NUMBER
            }
            .forEach { location: DescriptorProtos.SourceCodeInfo.Location ->
                val serviceNumber = location.getPath(1)
                val serviceProto =
                    fileProto.getService(serviceNumber)
                val ctx = extractServiceContext(protoTypeMap, serviceProto, locations, serviceNumber)
                ctx.packageName = extractPackageName(fileProto)
                ctx.protoName = fileProto.name
                ctx.javaDoc = getJavaDoc(getComments(location), SERVICE_JAVADOC_PREFIX)
                serviceContexts.add(ctx)
            }
        return serviceContexts
    }

    private fun extractPackageName(proto: DescriptorProtos.FileDescriptorProto): String? {
        val options = proto.options
        if (options != null) {
            val javaPackage = options.javaPackage
            if (!javaPackage.isNullOrEmpty()) {
                return javaPackage
            }
        }
        return proto.getPackage().ifEmpty { null }
    }

    private fun extractServiceContext(
        protoTypeMap: ProtoTypeMap,
        serviceProto: DescriptorProtos.ServiceDescriptorProto,
        locations: List<DescriptorProtos.SourceCodeInfo.Location>,
        serviceNumber: Int
    ): ServiceContext {
        val ctx = ServiceContext()
        ctx.fileName = serviceProto.name + CLASS_SUFFIX + ".java"
        ctx.className = serviceProto.name + CLASS_SUFFIX
        ctx.serviceName = serviceProto.name
        ctx.deprecated = serviceProto.options != null && serviceProto.options.deprecated
        locations.stream()
            .filter { location: DescriptorProtos.SourceCodeInfo.Location ->
                location.pathCount == METHOD_NUMBER_OF_PATHS && location.getPath(0) == DescriptorProtos.FileDescriptorProto.SERVICE_FIELD_NUMBER && location.getPath(
                    1
                ) == serviceNumber && location.getPath(2) == DescriptorProtos.ServiceDescriptorProto.METHOD_FIELD_NUMBER
            }
            .forEach { location: DescriptorProtos.SourceCodeInfo.Location ->
                val methodNumber = location.getPath(METHOD_NUMBER_OF_PATHS - 1)
                val methodProto =
                    serviceProto.getMethod(methodNumber)

                // Identify methods to generate a CompletableFuture-based client for.
                // Only unary methods are supported.
                if (!methodProto.clientStreaming && !methodProto.serverStreaming) {
                    with(MethodContext()) {
                        methodName = lowerCaseFirst(methodProto.name)
                        inputType = protoTypeMap.toJavaTypeName(methodProto.inputType)
                        outputType = protoTypeMap.toJavaTypeName(methodProto.outputType)
                        deprecated = methodProto.options != null && methodProto.options.deprecated
                        javaDoc = getJavaDoc(getComments(location), METHOD_JAVADOC_PREFIX)
                        ctx.methods.add(this)
                    }
                }
            }
        return ctx
    }

    private fun lowerCaseFirst(s: String): String {
        return s[0].lowercaseChar().toString() + s.substring(1)
    }

    private fun absoluteFileName(ctx: ServiceContext): String? {
        val dir = ctx.packageName!!.replace('.', '/')
        return if (dir.isEmpty()) {
            ctx.fileName
        } else {
            dir + "/" + ctx.fileName
        }
    }

    private fun buildFile(context: ServiceContext): PluginProtos.CodeGeneratorResponse.File {
        return makeFile(absoluteFileName(context)!!, applyTemplate("Jdk8Stub.mustache", context))
    }

    private fun getComments(location: DescriptorProtos.SourceCodeInfo.Location): String {
        return location.leadingComments.ifEmpty { location.trailingComments }
    }

    private fun getJavaDoc(comments: String, prefix: String): String? {
        if (comments.isNotEmpty()) {
            val builder = StringBuilder("/**\n").append(prefix).append(" * <pre>\n")

            comments
                .escapeHTML()
                .split("\n")
                .dropLastWhile { it.isEmpty() }
                .forEach { builder.append(prefix).append(" * ").append(it).append("\n") }
            builder
                .append(prefix).append(" * <pre>\n")
                .append(prefix).append(" */")
            return builder.toString()
        }
        return null
    }

    /**
     * Escape HTML String
     */
    private fun String.escapeHTML(): String {
        val text: String = this@escapeHTML
        if (text.isEmpty()) return text

        return buildString(length) {
            for (element in text) {
                when (val ch: Char = element) {
                    '\'' -> append("&apos;")
                    '\"' -> append("&quot")
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    else -> append(ch)
                }
            }
        }
    }

    /**
     * Backing class for mustache template.
     */
    private inner class ServiceContext {
        // CHECKSTYLE DISABLE VisibilityModifier FOR 8 LINES
        var fileName: String? = null
        var protoName: String? = null
        var packageName: String? = null
        var className: String? = null
        var serviceName: String? = null
        var deprecated = false
        var javaDoc: String? = null
        val methods: MutableList<MethodContext> = ArrayList()
    }

    /**
     * Backing class for mustache template.
     */
    private inner class MethodContext {
        // CHECKSTYLE DISABLE VisibilityModifier FOR 5 LINES
        var methodName: String? = null
        var inputType: String? = null
        var outputType: String? = null
        var deprecated = false
        var javaDoc: String? = null
    }

    companion object {
        private const val CLASS_SUFFIX = "Grpc8"
        private const val SERVICE_JAVADOC_PREFIX = "    "
        private const val METHOD_JAVADOC_PREFIX = "        "
        private const val METHOD_NUMBER_OF_PATHS = 4
    }
}
