package br.com.rodrigo.pix.registra

import br.com.rodrigo.*
import br.com.rodrigo.pix.TipoChave
import br.com.rodrigo.pix.TipoConta
import br.com.rodrigo.pix.exclusao.ExclusaoChaveDto
import br.com.rodrigo.util.handler.ExceptionHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ExceptionHandler
class RegistraChaveEndpoint(@Inject val chavePixService: ChavePixService) :
    KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    override fun registraChave(
        request: RegistraChaveRequest,
        responseObserver: StreamObserver<RegistraChaveResponse>
    ) {
        val chavePixDto = request.toChavePixDto()
        val chavePix = chavePixService.registra(chavePixDto)

        val response = RegistraChaveResponse.newBuilder()
            .setPixId(chavePix.id.toString())
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

}

fun RegistraChaveRequest.toChavePixDto(): ChavePixDto {

    return ChavePixDto(
        clienteId = this.identificadorCliente,
        tipo = when (this.tipoChaveMessage) {
            TipoChaveMessage.TIPO_CHAVE_INDEFINIDO -> null
            else -> TipoChave.valueOf(this.tipoChaveMessage.name)
        },
        tipoConta = when (this.tipoContaMessage) {
            TipoContaMessage.TIPO_CONTA_INDEFINIDO -> null
            else -> TipoConta.valueOf(this.tipoContaMessage.name)
        },
        chave = this.valorDaChave
    )
}
