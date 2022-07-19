package io.infinitic.pulsarRPC

import com.google.protobuf.GeneratedMessageV3
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.pulsarRPC.examples.helloworld.GreeterGrpc
import io.pulsarRPC.examples.helloworld.HelloReply
import io.pulsarRPC.examples.helloworld.HelloRequest

fun main(args: Array<String>) {
    val request = HelloRequest.newBuilder().setName("World").build()

    val channel = PulsarChannel()

    val stub: GreeterGrpc.GreeterBlockingStub = GreeterGrpc.newBlockingStub(channel)

    val r: HelloReply = stub.sayHello(request)

//    val requestObserver = GreeterGrpc.newStub(channel).sayHelloStreaming(HelloReplyObserver())
//
//    try {
//        // Send numPoints points randomly selected from the features list.
//        for (i in 0 until 10) {
//            val reply = HelloRequest.newBuilder().setName("$i!").build()
//
//            requestObserver.onNext(reply)
//
//            Thread.sleep(100)
//        }
//    } catch (e: RuntimeException) {
//        // Cancel RPC
//        requestObserver.onError(e)
//        throw e
//    }
//    requestObserver.onCompleted()

//    GreeterGrpc.newStub(channel).sayHello(request, HelloReplyObserver())

//    println(GreeterGrpc.newFutureStub(channel).sayHello(request).get())
}

class PulsarChannel(
    // pulsar client
) : Channel() {
    override fun <RequestT : Any, ResponseT : Any> newCall(
        methodDescriptor: MethodDescriptor<RequestT, ResponseT>,
        callOptions: CallOptions?
    ): ClientCall<RequestT, ResponseT> {
        System.err.println("PulsarChannel.newCall(methodDescriptor=$methodDescriptor, callOptions=$callOptions)")
        System.err.println("Calling ${methodDescriptor.serviceName}::${methodDescriptor.bareMethodName}")

        return PulsarClientCall()
    }

    override fun authority(): String {
        System.err.println("PulsarChannel.authority()")
        TODO("Not yet implemented")
    }
}

class PulsarClientCall<RequestT : Any, ResponseT : Any>(
    // topic producer
    // topic consumer
) : ClientCall<RequestT, ResponseT>() {

    override fun start(responseListener: Listener<ResponseT>, headers: Metadata?) {
        System.err.println("PulsarClientCall.start(responseListener=$responseListener, headers=$headers)")

        responseListener.onReady()

        responseListener.onHeaders(headers)

        // response
        val reply = HelloReply.newBuilder().setMessage("Hello World!").build()

        responseListener.onMessage(reply as ResponseT)

        responseListener.onClose(Status.OK, headers)
    }

    override fun request(numMessages: Int) {
        System.err.println("PulsarClientCall.request(numMessages=$numMessages)")
        // TODO("Not yet implemented")
    }

    override fun cancel(message: String?, cause: Throwable?) {
        System.err.println("PulsarClientCall.cancel(message=$message, cause=$cause)")
        // TODO("Not yet implemented")
    }

    override fun halfClose() {
        System.err.println("PulsarClientCall.halfClose()")
        // TODO("Not yet implemented")
    }

    override fun sendMessage(message: RequestT) {
        System.err.println("PulsarClientCall.sendMessage(message = $message)")
        System.err.println("with parameter: ${message::class.java} - $message)")
        // TODO("Not yet implemented")
        // send message RunTask(
        println((message as GeneratedMessageV3).toByteArray())
    }
}

class HelloReplyObserver<T : Any> : StreamObserver<T> {
    override fun onNext(value: T) {
        System.err.println("HelloReplyObserver.onNext: $value")
    }

    override fun onError(t: Throwable?) {
        System.err.println("HelloReplyObserver.onError: $t")
    }

    override fun onCompleted() {
        System.err.println("HelloReplyObserver.onCompleted")
    }
}
