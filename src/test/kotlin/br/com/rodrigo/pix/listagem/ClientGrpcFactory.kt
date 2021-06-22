package br.com.rodrigo.pix.listagem

import br.com.rodrigo.KeyManagerListaChavesGrpcServiceGrpc
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import javax.inject.Singleton

@Factory
class ClientGrpcFactory {

    @Singleton
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerListaChavesGrpcServiceGrpc.KeyManagerListaChavesGrpcServiceBlockingStub? {
        return KeyManagerListaChavesGrpcServiceGrpc.newBlockingStub(channel)
    }
}