package io.pulsarRPC.jprotoc.dump

import io.pulsarRPC.jprotoc.ProtocPlugin

fun main(args: Array<String>) {
    ProtocPlugin.generate(DumpGenerator())
}
