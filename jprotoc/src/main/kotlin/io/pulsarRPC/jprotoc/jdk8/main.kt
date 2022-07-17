package io.pulsarRPC.jprotoc.jdk8

import io.pulsarRPC.jprotoc.ProtocPlugin

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        ProtocPlugin.generate(Jdk8Generator())
    } else {
        ProtocPlugin.debug(Jdk8Generator(), args[0])
    }
}
