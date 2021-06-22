package br.com.rodrigo.pix.listagem

import br.com.rodrigo.*
import br.com.rodrigo.util.handler.ExceptionHandler
import com.google.protobuf.Timestamp
import com.google.protobuf.TimestampOrBuilder
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ExceptionHandler
class ListaChaveEndpoint(@Inject val service: ListaChaveService) :
    KeyManagerListaChavesGrpcServiceGrpc.KeyManagerListaChavesGrpcServiceImplBase() {

    override fun listaChaves(request: ListaChavesRequest, responseObserver: StreamObserver<ListaChavesResponse>) {

        val chavePixList = service.listaChave(request.identificadorCliente)

        //Pegando cada chavePix da lista chavePixList,
        // criando uma ChavePixMessage com os dados da chavePix e
        // armazenando-as na lista chaves, que é uma lista de ChavePixMessage
        // essa lista será colocada no Objeto ListaChavesResponse
        val chaves = chavePixList.map {
            ChavePixMessage.newBuilder()
                .setPixId(it.id.toString())
                .setTipoChaveMessage(TipoChaveMessage.valueOf(it.tipoChave.name))
                .setValor(it.chave)
                .setTipoContaMessage(TipoContaMessage.valueOf(it.tipoConta.name))
                .setCriadoEm(it.criadaEm.let {
                    val dataCriacao = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(dataCriacao.epochSecond)
                        .setNanos(dataCriacao.nano)
                        .build()
                })
                .build()
        }

        val response = ListaChavesResponse.newBuilder()
            .setIdentificadorCliente(request.identificadorCliente)
            .addAllChaves(chaves)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}