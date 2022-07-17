package io.pulsarRPC.jprotoc

import com.google.protobuf.DescriptorProtos
import java.util.function.Consumer

/**
 * `ProtoTypeMap` maintains a dictionary for looking up Java type names when given proto types.
 */
class ProtoTypeMap private constructor(val types: Map<String, String>) {

    /**
     * Returns the full Java type name for the given proto type.
     *
     * @param protoTypeName the proto type to be converted to a Java type
     */
    fun toJavaTypeName(protoTypeName: String): String = types[protoTypeName]!!

    companion object {
        private fun dotJoin(vararg ts: Any?) = listOfNotNull(ts).joinToString(".")

        /**
         * Returns an instance of [ProtoTypeMap] based on the given FileDescriptorProto instances.
         *
         * @param fileDescriptorProtos the full collection of files descriptors from the code generator request
         */
        fun of(fileDescriptorProtos: Collection<DescriptorProtos.FileDescriptorProto>): ProtoTypeMap {

            require(fileDescriptorProtos.isNotEmpty()) { "fileDescriptorProtos must not be empty" }

            val types = mutableMapOf<String, String>()

            for (fileDescriptor in fileDescriptorProtos) {
                val fileOptions = fileDescriptor.options
                val protoPackage = if (fileDescriptor.hasPackage()) ("." + fileDescriptor.getPackage()) else ""
                val javaPackage: String? = when (fileOptions.hasJavaPackage()) {
                    true -> fileOptions.javaPackage
                    false -> fileDescriptor.getPackage()
                }.ifEmpty { null }

                val enclosingClassName: String? = when (fileOptions.javaMultipleFiles) {
                    true -> null
                    else -> getJavaOuterClassname(fileDescriptor, fileOptions)
                }

                // Identify top-level enums
                fileDescriptor.enumTypeList.forEach(
                    Consumer {
                        types[protoPackage + "." + it.name] = dotJoin(javaPackage, enclosingClassName, it.name)
                    }
                )

                // Identify top-level messages, and nested types
                fileDescriptor.messageTypeList.forEach(
                    Consumer {
                        recursivelyAddTypes(
                            types,
                            it,
                            protoPackage,
                            enclosingClassName,
                            javaPackage
                        )
                    }
                )
            }
            return ProtoTypeMap(types)
        }

        private fun recursivelyAddTypes(
            types: MutableMap<String, String>,
            m: DescriptorProtos.DescriptorProto,
            protoPackage: String,
            enclosingClassName: String?,
            javaPackage: String?
        ) {
            // Identify current type
            val protoTypeName = protoPackage + "." + m.name
            types[protoTypeName] = dotJoin(javaPackage, enclosingClassName, m.name)

            // Identify any nested Enums
            m.enumTypeList.forEach(
                Consumer {
                    types[protoPackage + "." + m.name + "." + it.name] = dotJoin(javaPackage, enclosingClassName, m.name, it.name)
                }
            )

            // Recursively identify any nested types
            m.nestedTypeList.forEach(
                Consumer {
                    recursivelyAddTypes(
                        types,
                        it,
                        protoPackage + "." + m.name,
                        dotJoin(enclosingClassName, m.name),
                        javaPackage
                    )
                }
            )
        }

        private fun getJavaOuterClassname(
            fileDescriptor: DescriptorProtos.FileDescriptorProto,
            fileOptions: DescriptorProtos.FileOptions
        ): String {
            if (fileOptions.hasJavaOuterClassname()) {
                return fileOptions.javaOuterClassname
            }

            // If the outer class name is not explicitly defined, then we take the proto filename, strip its extension,
            // and convert it from snake case to camel case.
            var filename = fileDescriptor.name.substring(0, fileDescriptor.name.length - ".proto".length)

            // Protos in subdirectories without java_outer_classname have their path prepended to the filename. Remove
            // if present.
            if (filename.contains("/")) {
                filename = filename.substring(filename.lastIndexOf('/') + 1)
            }
            filename = makeInvalidCharactersUnderscores(filename)
            filename = convertToCamelCase(filename)
            filename = appendOuterClassSuffix(filename, fileDescriptor)

            return filename
        }

        /**
         * In the event of a name conflict between the outer and inner type names, protoc adds an OuterClass suffix to the
         * outer type's name.
         */
        private fun appendOuterClassSuffix(
            enclosingClassName: String,
            fd: DescriptorProtos.FileDescriptorProto
        ): String {
            return if (fd.enumTypeList.any { it.name == enclosingClassName } ||
                fd.messageTypeList.any { it.name == enclosingClassName } ||
                fd.serviceList.any { it.name == enclosingClassName }
            ) {
                enclosingClassName + "OuterClass"
            } else {
                enclosingClassName
            }
        }

        /**
         * Replace invalid proto identifier characters with an underscore, so they will be dropped and camel cases below.
         * https://developers.google.com/protocol-buffers/docs/reference/proto3-spec
         */
        private fun makeInvalidCharactersUnderscores(fileName: String): String {
            val fileChars = fileName.toCharArray()
            for (i in fileChars.indices) {
                val c = fileChars[i]
                if (!c.isLetterOrDigit()) { fileChars[i] = '_' }
            }
            return String(fileChars)
        }

        /**
         * Adjust a class name to follow the JavaBean spec.
         * - capitalize the first letter
         * - remove embedded underscores & capitalize the following letter
         * - capitalize letter after a number
         *
         * @param name method name
         * @return lower name
         */
        private fun convertToCamelCase(name: String): String {
            val sb = StringBuilder()
            sb.append(name[0].uppercaseChar())
            for (i in 1 until name.length) {
                val c = name[i]
                val prev = name[i - 1]
                if (c != '_') {
                    if (prev == '_' || prev.isDigit()) {
                        sb.append(c.uppercaseChar())
                    } else {
                        sb.append(c)
                    }
                }
            }
            return sb.toString()
        }
    }
}
