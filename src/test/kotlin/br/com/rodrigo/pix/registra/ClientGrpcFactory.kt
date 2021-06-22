package br.com.rodrigo.pix.registra

import br.com.rodrigo.KeyManagerGrpcServiceGrpc
import br.com.rodrigo.KeyManagerRemoveGrpcServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.stub.AbstractBlockingStub
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import javax.inject.Singleton

@Factory
class ClientGrpcFactory {

    @Singleton
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub? {
        return KeyManagerGrpcServiceGrpc.newBlockingStub(channel)

    }

}

