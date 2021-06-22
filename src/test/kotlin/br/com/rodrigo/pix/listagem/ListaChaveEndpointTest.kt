package br.com.rodrigo.pix.listagem

import br.com.rodrigo.KeyManagerListaChavesGrpcServiceGrpc
import br.com.rodrigo.ListaChavesRequest
import br.com.rodrigo.pix.*
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest(transactional = false)
internal class ListaChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcCliente: KeyManagerListaChavesGrpcServiceGrpc.KeyManagerListaChavesGrpcServiceBlockingStub
) {

    @Test
    fun `deve retornar as 3 chaves do cliente`() {

        //CENARIO
        repository.deleteAll()
        val idClienteRodrigo = UUID.randomUUID()
        val dadosBancarios = DadosBancarios(agencia = "123", numero = "123", "Itau", titularNome = "Rodrigo", cpf = "99999999999")

        val chavePixEmail = ChavePix(identificadorCliente = idClienteRodrigo, tipoChave = TipoChave.EMAIL,
        tipoConta = TipoConta.CONTA_CORRENTE, chave = "rodrigo@zup.com.br", dadosBancario = dadosBancarios)

        val chavePixCPF = ChavePix(identificadorCliente = idClienteRodrigo, tipoChave = TipoChave.CPF,
            tipoConta = TipoConta.CONTA_CORRENTE, chave = "12345678958", dadosBancario = dadosBancarios)

        val chavePixCelular = ChavePix(identificadorCliente = idClienteRodrigo, tipoChave = TipoChave.CELULAR,
            tipoConta = TipoConta.CONTA_CORRENTE, chave = "+5535999990000", dadosBancario = dadosBancarios)

        //Salvando as 3 chaves para o mesmo cliente
        repository.saveAll(listOf(chavePixEmail, chavePixCPF, chavePixCelular))

        val request = ListaChavesRequest.newBuilder()
            .setIdentificadorCliente(idClienteRodrigo.toString())
            .build()

        //AÇÃO
        val response = grpcCliente.listaChaves(request)

        //VERIFICAÇÃO
        Assertions.assertEquals(3, response.chavesList.size)
        Assertions.assertEquals(idClienteRodrigo.toString(), response.identificadorCliente)
        Assertions.assertEquals("rodrigo@zup.com.br", response.chavesList[0].valor)
        Assertions.assertEquals("12345678958", response.chavesList[1].valor)
        Assertions.assertEquals("+5535999990000", response.chavesList[2].valor)
    }

    @Test
    fun `deve retornar uma lista vazia`() {

        //CENARIO
        repository.deleteAll()
        val idClienteSemChavePix = UUID.randomUUID()

        val request = ListaChavesRequest.newBuilder()
            .setIdentificadorCliente(idClienteSemChavePix.toString())
            .build()

        //AÇÃO
        val response = grpcCliente.listaChaves(request)

        //VERIFICAÇÃO
        Assertions.assertTrue(response.chavesList.isEmpty())
        Assertions.assertEquals(idClienteSemChavePix.toString(), response.identificadorCliente)

    }

    @Test
    fun `deve ser lancado um excpetion se o id do cliente for vazio`() {

        //CENARIO
        repository.deleteAll()

        val request = ListaChavesRequest.newBuilder()
            .setIdentificadorCliente("")
            .build()

        //AÇÃO
        val error = Assertions.assertThrows(StatusRuntimeException::class.java){
            grpcCliente.listaChaves(request)
        }

        //VERIFICAÇÃO
        Assertions.assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)

    }

    @Test
    fun `deve ser lancado um excpetion se o id do cliente for nulo`() {

        //CENARIO
        repository.deleteAll()

        val request = ListaChavesRequest.newBuilder()
            .build()

        //AÇÃO
        val error = Assertions.assertThrows(StatusRuntimeException::class.java){
            grpcCliente.listaChaves(request)
        }

        //VERIFICAÇÃO
        Assertions.assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)

    }

    @Test
    fun `deve ser lancado um excpetion se o id do cliente nao for do foarmato uuid`() {

        //CENARIO
        repository.deleteAll()

        val request = ListaChavesRequest.newBuilder()
            .setIdentificadorCliente("Nao-Formato-Uuid")
            .build()

        //AÇÃO
        val error = Assertions.assertThrows(StatusRuntimeException::class.java){
            grpcCliente.listaChaves(request)
        }

        //VERIFICAÇÃO
        Assertions.assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)

    }
}