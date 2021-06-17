package br.com.rodrigo.pix.exclusao

import br.com.rodrigo.KeyManagerRemoveGrpcServiceGrpc
import br.com.rodrigo.RemoveChaveRequest
import br.com.rodrigo.pix.ChavePix
import br.com.rodrigo.pix.ChavePixRepository
import br.com.rodrigo.pix.TipoChave
import br.com.rodrigo.pix.TipoConta
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest(transactional = false)
internal class ExclusaoChaveEndpointTest(
    val grpcClient: KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub,
    val repository: ChavePixRepository
) {

    @Test
    fun `deve remover uma chave pix`() {

        //CENARIO
        repository.deleteAll()
        val clienteId = UUID.randomUUID()
        val chavePix = ChavePix(clienteId, TipoChave.EMAIL, TipoConta.CONTA_CORRENTE, "rodrigo@zup.com.br")
        repository.save(chavePix)

        val request = RemoveChaveRequest.newBuilder()
            .setIdentificadorCliente(clienteId.toString())
            .setPixId(chavePix.id.toString())
            .build()

        //VERIFICACAO COM AÇÃO
        assertTrue(repository.existsByChave(chavePix.chave))
        assertTrue(repository.existsById(chavePix.id))
        grpcClient.removeChave(request)

        //VERIFICAÇAO
        assertFalse(repository.existsByChave(chavePix.chave))
        assertFalse(repository.existsById(chavePix.id))
    }

    @Test
    fun `deve ser lancada uma exception ao tentar remover uma chave inexistente`() {

        //CENARIO
        repository.deleteAll()
        val request = RemoveChaveRequest.newBuilder()
            .setIdentificadorCliente(UUID.randomUUID().toString())
            .setPixId(UUID.randomUUID().toString())
            .build()

        //AÇÃO
        val erro = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.removeChave(request)
        }

        //VERIFICAO
        assertEquals(Status.NOT_FOUND.code, erro.status.code)
    }

    @Test
    fun `deve ser lancada uma exception quando um usuario tentar remover uma chave que nao seja sua`() {

        //CENARIO
        repository.deleteAll()
        val cliente1 = UUID.randomUUID()
        val cliente2 = UUID.randomUUID()
        val chavePix = ChavePix(cliente1,TipoChave.EMAIL,TipoConta.CONTA_CORRENTE,"rodrigo@gmail.com")
        repository.save(chavePix)

        val request = RemoveChaveRequest.newBuilder()
            .setIdentificadorCliente(cliente2.toString())
            .setPixId(chavePix.id.toString())
            .build()

        //AÇÃO
        val erro = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.removeChave(request)
        }

        //VERIFICAO
        assertEquals(Status.FAILED_PRECONDITION.code, erro.status.code)
    }

    @Test
    fun `deve ser lancada uma exception ao tentar remover chave sem passar seu valor`() {

        //CENARIO
        repository.deleteAll()
        val cliente = UUID.randomUUID()
        val chavePix = ChavePix(cliente,TipoChave.EMAIL,TipoConta.CONTA_CORRENTE,"rodrigo@gmail.com")
        repository.save(chavePix)

        val request = RemoveChaveRequest.newBuilder()
            .setIdentificadorCliente(cliente.toString())
            .build()

        //AÇÃO
        val erro = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.removeChave(request)
        }

        //VERIFICAO
        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)
    }

    @Test
    fun `deve ser lancada uma exception ao tentar remover chave sem passar cliente id`() {

        //CENARIO
        repository.deleteAll()
        val cliente = UUID.randomUUID()
        val chavePix = ChavePix(cliente,TipoChave.EMAIL,TipoConta.CONTA_CORRENTE,"rodrigo@gmail.com")
        repository.save(chavePix)

        val request = RemoveChaveRequest.newBuilder()
            .setPixId(chavePix.id.toString())
            .build()

        //AÇÃO
        val erro = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.removeChave(request)
        }

        //VERIFICAO
        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)
    }

    @Test
    fun `deve ser lancada uma exception se id do cliente nao for padrao uuid`() {

        //CENARIO
        repository.deleteAll()
        val cliente = UUID.randomUUID()
        val chavePix = ChavePix(cliente,TipoChave.EMAIL,TipoConta.CONTA_CORRENTE,"rodrigo@gmail.com")
        repository.save(chavePix)

        val request = RemoveChaveRequest.newBuilder()
            .setIdentificadorCliente("123456")
            .setPixId(chavePix.id.toString())
            .build()

        //AÇÃO
        val erro = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.removeChave(request)
        }

        //VERIFICAO
        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)
    }

    @Test
    fun `deve ser lancada uma exception se id da chave nao for padrao uuid`() {

        //CENARIO
        repository.deleteAll()
        val cliente = UUID.randomUUID()
        val chavePix = ChavePix(cliente,TipoChave.EMAIL,TipoConta.CONTA_CORRENTE,"rodrigo@gmail.com")
        repository.save(chavePix)

        val request = RemoveChaveRequest.newBuilder()
            .setIdentificadorCliente(cliente.toString())
            .setPixId("123456")
            .build()

        //AÇÃO
        val erro = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.removeChave(request)
        }

        //VERIFICAO
        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)
    }

}

