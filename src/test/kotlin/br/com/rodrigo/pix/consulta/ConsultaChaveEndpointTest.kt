package br.com.rodrigo.pix.consulta

import br.com.rodrigo.ConsultaChaveRequest
import br.com.rodrigo.KeyManagerConsultaGrpcServiceGrpc
import br.com.rodrigo.integracao.bcb.BcbClient
import br.com.rodrigo.integracao.bcb.classes.*
import br.com.rodrigo.pix.*
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class ConsultaChaveEndpointTest(
    @Inject val repository: ChavePixRepository,
    @Inject val grpcClient: KeyManagerConsultaGrpcServiceGrpc.KeyManagerConsultaGrpcServiceBlockingStub
) {

    @field:Inject
    lateinit var bcbClient: BcbClient

    @Test
    fun `deve encontrar uma chave pix atraves do pixId e do clientId`() {

        //Cenario
        repository.deleteAll()

        val clienteId = UUID.randomUUID()
        val dadosBancarios = DadosBancarios("123", "123", "Itau", "Yuri", "99999999999")
        val chavePix = ChavePix(
            identificadorCliente = clienteId,
            tipoChave = TipoChave.EMAIL,
            tipoConta = TipoConta.CONTA_CORRENTE,
            chave = "yuri@zup.com.br",
            dadosBancario = dadosBancarios
        )

        repository.save(chavePix)

        //Ação
        //Para criar um request de Oneof, primeiro se cria o atributo de oneof!
        val request = ConsultaChaveRequest.newBuilder()
            .setFiltroPorIds(
                ConsultaChaveRequest.FiltroPorIds.newBuilder()
                    .setIdentificadorCliente(clienteId.toString())
                    .setPixId(chavePix.id.toString())
                    .build()
            )
            .build()

        val response = grpcClient.consultaChave(request)

        //Verificação
        Assertions.assertEquals(response.valorDaChave, chavePix.chave)

    }

    @Test
    fun `deve encontrar uma chave pix em nosso sistema atraves do seu valor`() {

        //Cenario
        repository.deleteAll()

        val clienteId = UUID.randomUUID()
        val dadosBancarios = DadosBancarios("123", "123", "Itau", "Yuri", "99999999999")
        val chavePix = ChavePix(
            identificadorCliente = clienteId,
            tipoChave = TipoChave.EMAIL,
            tipoConta = TipoConta.CONTA_CORRENTE,
            chave = "yuri@zup.com.br",
            dadosBancario = dadosBancarios
        )

        repository.save(chavePix)

        //Ação
        //Para criar um request de Oneof, primeiro se cria o atributo de oneof!
        val request = ConsultaChaveRequest.newBuilder()
            .setChave(chavePix.chave)
            .build()

        val response = grpcClient.consultaChave(request)

        //Verificação
        Assertions.assertEquals(response.valorDaChave, chavePix.chave)
        Assertions.assertEquals(response.identificadorCliente, chavePix.identificadorCliente.toString())
        Assertions.assertEquals(response.pixId, chavePix.id.toString())

    }

    @Test
    fun `deve encontrar uma chave pix no sistema do bcb atraves do seu valor`() {

        //Cenario
        repository.deleteAll()

        /**
         * Não salvei a chave pix em nosso sistema e vou fazer o BCB responder que encontrou a chave em seu sistema.
         */

        val bcbResponse = PixKeyDetailsResponse(
            keyType = KeyType.EMAIL, key = "yuri@zup.com.br",
            bankAccount = BankAccount(DadosBancarios.ITAU_UNIBANCO_ISPB, "123", "123", AccountType.CACC),
            Owner(TypeOwner.NATURAL_PERSON, "Yuri", "99999999999"), LocalDateTime.now()
        )

        Mockito.`when`(bcbClient.busca(bcbResponse.key)).thenReturn(HttpResponse.ok(bcbResponse))

        //Ação
        val request = ConsultaChaveRequest.newBuilder()
            .setChave(bcbResponse.key)
            .build()

        val response = grpcClient.consultaChave(request)

        //Verificação
        Assertions.assertEquals(response.valorDaChave, bcbResponse.key)

    }

    @Test
    fun `nao deve encontrar uma chave pix atraves do pixId e do clientId`() {

        //Cenario
        repository.deleteAll()

        val clienteId = UUID.randomUUID()
        val chavePixId = UUID.randomUUID()


        //Ação
        //Para criar um request de Oneof, primeiro se cria o atributo de oneof!
        val request = ConsultaChaveRequest.newBuilder()
            .setFiltroPorIds(
                ConsultaChaveRequest.FiltroPorIds.newBuilder()
                    .setIdentificadorCliente(clienteId.toString())
                    .setPixId(chavePixId.toString())
                    .build()
            )
            .build()

        val erro = Assertions.assertThrows(StatusRuntimeException::class.java) {
            grpcClient.consultaChave(request)
        }

        //Verificação
        Assertions.assertEquals(Status.NOT_FOUND.code, erro.status.code)

    }

    @Test
    fun `nao deve encontrar uma chave pix`() {

        //Cenario
        repository.deleteAll()

        /**
         * Não salvei a chave pix em nosso sistema e vou fazer o BCB responder que também não encontrou a chave em seu sistema.
         */


        Mockito.`when`(bcbClient.busca("naoexiste@gmail.com.br")).thenReturn(HttpResponse.badRequest())

        //Ação
        val request = ConsultaChaveRequest.newBuilder()
            .setChave("naoexiste@gmail.com.br")
            .build()

        val erro = Assertions.assertThrows(StatusRuntimeException::class.java) {
            grpcClient.consultaChave(request)
        }

        //Verificação
        Assertions.assertEquals(Status.NOT_FOUND.code, erro.status.code)

    }

    @Test
    fun `deve ser lancada uma exception quando ocorrer uma requisicao sem os preenchimento dos dados`(){

        //Cenario
        repository.deleteAll()

        //Acao
        val erro = Assertions.assertThrows(StatusRuntimeException::class.java){
            grpcClient.consultaChave(null)
        }

        //Verificacao
        Assertions.assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)

    }

    @MockBean(BcbClient::class)
    fun mockBcbClient(): BcbClient {
        return Mockito.mock((BcbClient::class.java))
    }

}


