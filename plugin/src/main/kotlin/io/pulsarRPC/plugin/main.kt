package io.pulsarRPC.plugin

import com.salesforce.jprotoc.ProtocPlugin

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        ProtocPlugin.generate(Plugin())
    } else {
        ProtocPlugin.debug(Plugin(), args[0])
    }
}
