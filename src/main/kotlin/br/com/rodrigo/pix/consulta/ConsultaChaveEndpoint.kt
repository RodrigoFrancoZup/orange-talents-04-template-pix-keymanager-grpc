package br.com.rodrigo.pix.consulta

import br.com.rodrigo.*
import br.com.rodrigo.integracao.bcb.BcbClient
import br.com.rodrigo.pix.ChavePixRepository
import br.com.rodrigo.util.handler.ExceptionHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException
import javax.validation.Validator

@Singleton
@ExceptionHandler
class ConsultaChaveEndpoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val bcbClient: BcbClient,
    @Inject private val validator: Validator,
) : KeyManagerConsultaGrpcServiceGrpc.KeyManagerConsultaGrpcServiceImplBase() {

    override fun consultaChave(
        request: ConsultaChaveRequest,
        responseObserver: StreamObserver<ConsultaChaveResponse>
    ) {

        val filtro = request.toModel(validator)
        val chavePixInfo = filtro.filtra(repository, bcbClient)

        val contaMessage = ContaMessage.newBuilder()
            .setInstituicao(chavePixInfo.dadosBancarios.instituicao)
            .setAgencia(chavePixInfo.dadosBancarios.agencia)
            .setNumero(chavePixInfo.dadosBancarios.numero)
            .setTipoContaMessage(TipoContaMessage.valueOf(chavePixInfo.tipoConta.name))
            .build()

        val titular = TitularMessage.newBuilder()
            .setCpf(chavePixInfo.dadosBancarios.cpf)
            .setNome(chavePixInfo.dadosBancarios.titularNome)
            .build()

        //Peguei a data e horario que estava salvo, fiz ele virar Instante seguindo o UTC.
        val registradaEm = chavePixInfo.registradaEm.atZone(ZoneId.of("UTC")).toInstant()

        //Atraves de registradaEm vou gerar um Timestamp do google
        val criadoEm = Timestamp.newBuilder()
            .setNanos(registradaEm.nano)
            .setSeconds(registradaEm.epochSecond)
            .build()

        val response = ConsultaChaveResponse.newBuilder()
            .setIdentificadorCliente(chavePixInfo.clienteId.toString() ?: "")
            .setPixId(chavePixInfo.pixId.toString() ?: "")
            .setValorDaChave(chavePixInfo.chave)
            .setTipoChaveMessage(TipoChaveMessage.valueOf(chavePixInfo.tipoChave.name))
            .setConta(contaMessage)
            .setCriadoEm(criadoEm)
            .setTitular(titular)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}

fun ConsultaChaveRequest.toModel(validator: Validator): Filtro {

    //filtroCase é campo interno do oneof que usamos, atraves dele sabemos qual campo foi preenchido
    val filtro = when (filtroCase!!) {

        //Verfificando se foi preenchido o FiltroPorIds
        ConsultaChaveRequest.FiltroCase.FILTROPORIDS -> filtroPorIds.let {
            Filtro.PorFiltroIds(clienteId = it.identificadorCliente, pixId = it.pixId)
        }

        //Verfificando se foi preenchido o Chave
        ConsultaChaveRequest.FiltroCase.CHAVE -> Filtro.PorChave(chave)

        //Verfificando se nenhum foi preenchido
        ConsultaChaveRequest.FiltroCase.FILTRO_NOT_SET -> Filtro.Invalido()
    }

    //Validando o objeto filtro seguindo suas anotações do Bean Validation
    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations);
    }

    return filtro
}