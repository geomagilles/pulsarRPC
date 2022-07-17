package io.pulsarRPC.jprotoc

/**
 * This exception represents a structural problem with output generation. Error messages will be printed to the
 * console output.
 */
class GeneratorException(message: String?) : RuntimeException(message)
