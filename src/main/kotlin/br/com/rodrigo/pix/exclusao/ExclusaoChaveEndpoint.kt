package br.com.rodrigo.pix.exclusao

import br.com.rodrigo.KeyManagerRemoveGrpcServiceGrpc
import br.com.rodrigo.RegistraChaveResponse
import br.com.rodrigo.RemoveChaveRequest
import br.com.rodrigo.util.handler.ExceptionHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ExceptionHandler
class ExclusaoChaveEndpoint(@Inject val exclusaoService: ChaveExclusaoService) :
    KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceImplBase() {

    override fun removeChave(request: RemoveChaveRequest, responseObserver: StreamObserver<RegistraChaveResponse>) {

        val exclusaoChaveDto = request.toExclusaoChaveDto()
        val idChaveRemovida = exclusaoService.removeChavePix(exclusaoChaveDto)

        responseObserver.onNext(
            RegistraChaveResponse.newBuilder()
                .setPixId(idChaveRemovida)
                .build()
        )
        responseObserver.onCompleted()
    }
}


fun RemoveChaveRequest.toExclusaoChaveDto(): ExclusaoChaveDto {
    return ExclusaoChaveDto(this.identificadorCliente, this.pixId)
}
