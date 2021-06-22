package br.com.rodrigo.pix.registra

import br.com.rodrigo.KeyManagerGrpcServiceGrpc
import br.com.rodrigo.RegistraChaveRequest
import br.com.rodrigo.TipoChaveMessage
import br.com.rodrigo.TipoContaMessage
import br.com.rodrigo.integracao.bcb.BcbClient
import br.com.rodrigo.integracao.bcb.classes.*
import br.com.rodrigo.integracao.itau.ContasItauClient
import br.com.rodrigo.integracao.itau.ContasItauResponse
import br.com.rodrigo.integracao.itau.InstituicaoResponse
import br.com.rodrigo.integracao.itau.TitularResponse
import br.com.rodrigo.pix.*
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject


@MicronautTest(transactional = false)
internal class RegistraChaveEndpointTest(
    val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub,
    val repository: ChavePixRepository
) {

    @field:Inject
    lateinit var itauClient: ContasItauClient

    @field:Inject
    lateinit var bcbClient: BcbClient

    @Test
    fun `deve adicionar uma nova chave pix`() {

        //Cenário
        repository.deleteAll()
        val request = RegistraChaveRequest.newBuilder()
            .setIdentificadorCliente("bc35591d-b547-4151-a325-4a9d2cd19614")
            .setTipoChaveMessage(TipoChaveMessage.EMAIL)
            .setTipoContaMessage(TipoContaMessage.CONTA_CORRENTE)
            .setValorDaChave("mentor@zup.com.br")
            .build()

        val requestBcb = CreatePixKeyRequest(
            keyType = KeyType.EMAIL, key = "mentor@zup.com.br",
            bankAccount = BankAccount(
                participant = DadosBancarios.ITAU_UNIBANCO_ISPB,
                branch = "123",
                accountNumber = "123",
                accountType = AccountType.CACC
            ),
            owner = Owner(type = TypeOwner.NATURAL_PERSON, name = "Rodrigo", taxIdNumber = "99999999999")
        )

        val responseBcb = CreatePixKeyResponse(
            keyType = KeyType.EMAIL, key = "mentor@zup.com.br",
            bankAccount = BankAccount(
                participant = DadosBancarios.ITAU_UNIBANCO_ISPB,
                branch = "123",
                accountNumber = "123",
                accountType = AccountType.CACC
            ),
            owner = Owner(type = TypeOwner.NATURAL_PERSON, name = "Rodrigo", taxIdNumber = "99999999999"),
            createdAt = LocalDateTime.now()
        )

        val responseItauClient = ContasItauResponse(
            TipoConta.CONTA_CORRENTE, "123", "123",
            InstituicaoResponse("Itau", "789"),
            TitularResponse(UUID.randomUUID(), "Rodrigo", "99999999999")
        )

        //Pense ao Mockar: ao executar o teste o programa está rodando de maneira "real", fora do ambiente de teste, quando for para fazer a busca no sistema itau,
        // a execução será feita seguindo o código a seguir, logo o resultado será OK!
        // está sendo simulado!
        Mockito.`when`(
            itauClient.buscaContaPorClienteETipo(
                request.identificadorCliente,
                request.tipoContaMessage.name
            )
        ).thenReturn(HttpResponse.ok(responseItauClient))

        Mockito.`when`(
            bcbClient.cadastra(requestBcb)
        ).thenReturn(HttpResponse.created(responseBcb))


        //Ação
        val response = grpcClient.registraChave(request)

        //Verificação
        assertNotNull(response.pixId)
        assertTrue(repository.existsByChave(request.valorDaChave))
        assertTrue(repository.existsById(UUID.fromString(response.pixId)))

    }

    @Test
    fun `deve adicionar uma chave pix aleatoria`() {

        //Cenário
        repository.deleteAll()
        val request = RegistraChaveRequest.newBuilder()
            .setIdentificadorCliente("bc35591d-b547-4151-a325-4a9d2cd19614")
            .setTipoChaveMessage(TipoChaveMessage.ALEATORIA)
            .setTipoContaMessage(TipoContaMessage.CONTA_CORRENTE)
            .setValorDaChave("")
            .build()

        val requestBcb = CreatePixKeyRequest(
            keyType = KeyType.RANDOM, key = "bb0e58f7-2b1c-4ce5-b3ac-2867fa665fc2",
            bankAccount = BankAccount(
                participant = DadosBancarios.ITAU_UNIBANCO_ISPB,
                branch = "123",
                accountNumber = "123",
                accountType = AccountType.CACC
            ),
            owner = Owner(type = TypeOwner.NATURAL_PERSON, name = "Rodrigo", taxIdNumber = "99999999999")
        )

        val responseBcb = CreatePixKeyResponse(
            keyType = KeyType.RANDOM, key = UUID.randomUUID().toString(),
            bankAccount = BankAccount(
                participant = DadosBancarios.ITAU_UNIBANCO_ISPB,
                branch = "123",
                accountNumber = "123",
                accountType = AccountType.CACC
            ),
            owner = Owner(type = TypeOwner.NATURAL_PERSON, name = "Rodrigo", taxIdNumber = "99999999999"),
            createdAt = LocalDateTime.now()
        )

        val responseItauClient = ContasItauResponse(
            TipoConta.CONTA_CORRENTE, "123", "123",
            InstituicaoResponse("Itau", "789"),
            TitularResponse(UUID.randomUUID(), "Rodrigo", "99999999999")
        )


        Mockito.`when`(
            itauClient.buscaContaPorClienteETipo(
                request.identificadorCliente,
                request.tipoContaMessage.name
            )
        ).thenReturn(HttpResponse.ok(responseItauClient))

        Mockito.`when`(
            bcbClient.cadastra(requestBcb)
        ).thenReturn(HttpResponse.created(responseBcb))

        //Ação
        val response = grpcClient.registraChave(request)

        //Verificação
        assertNotNull(response.pixId)
        assertTrue(repository.existsById(UUID.fromString(response.pixId)))
    }

    @Test
    fun `deve gerar uma exception ao tentar cadastrar uma chave pix sendo que a mesma nao foi cadastrada no bcb`() {

        //Cenário
        repository.deleteAll()
        val request = RegistraChaveRequest.newBuilder()
            .setIdentificadorCliente("bc35591d-b547-4151-a325-4a9d2cd19614")
            .setTipoChaveMessage(TipoChaveMessage.EMAIL)
            .setTipoContaMessage(TipoContaMessage.CONTA_CORRENTE)
            .setValorDaChave("mentor@zup.com.br")
            .build()

        val requestBcb = CreatePixKeyRequest(
            keyType = KeyType.EMAIL, key = "mentor@zup.com.br",
            bankAccount = BankAccount(
                participant = DadosBancarios.ITAU_UNIBANCO_ISPB,
                branch = "123",
                accountNumber = "123",
                accountType = AccountType.CACC
            ),
            owner = Owner(type = TypeOwner.NATURAL_PERSON, name = "Rodrigo", taxIdNumber = "99999999999")
        )

        val responseBcb = CreatePixKeyResponse(
            keyType = KeyType.EMAIL, key = "mentor@zup.com.br",
            bankAccount = BankAccount(
                participant = DadosBancarios.ITAU_UNIBANCO_ISPB,
                branch = "123",
                accountNumber = "123",
                accountType = AccountType.CACC
            ),
            owner = Owner(type = TypeOwner.NATURAL_PERSON, name = "Rodrigo", taxIdNumber = "99999999999"),
            createdAt = LocalDateTime.now()
        )

        val responseItauClient = ContasItauResponse(
            TipoConta.CONTA_CORRENTE, "123", "123",
            InstituicaoResponse("Itau", "789"),
            TitularResponse(UUID.randomUUID(), "Rodrigo", "99999999999")
        )

        Mockito.`when`(
            itauClient.buscaContaPorClienteETipo(
                request.identificadorCliente,
                request.tipoContaMessage.name
            )
        ).thenReturn(HttpResponse.ok(responseItauClient))

        Mockito.`when`(
            bcbClient.cadastra(requestBcb)
        ).thenReturn(HttpResponse.badRequest())


        //Ação
        val erro = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.registraChave(request)
        }

        //Verificação
        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)
    }


    @Test
    fun `deve gerar uma exception ao cadastar chave email incorreto`() {
        //Cenário
        repository.deleteAll()
        val request = RegistraChaveRequest.newBuilder()
            .setIdentificadorCliente("bc35591d-b547-4151-a325-4a9d2cd19614")
            .setTipoChaveMessage(TipoChaveMessage.EMAIL)
            .setTipoContaMessage(TipoContaMessage.CONTA_CORRENTE)
            .setValorDaChave("mentorzup.com.br")
            .build()

        val responseItauClient = ContasItauResponse(
            TipoConta.CONTA_CORRENTE, "123", "123",
            InstituicaoResponse("Itau", "789"),
            TitularResponse(UUID.randomUUID(), "Rodrigo", "99999999999")
        )
        val requestBcb = CreatePixKeyRequest(
            keyType = KeyType.RANDOM, key = "",
            bankAccount = BankAccount(
                participant = DadosBancarios.ITAU_UNIBANCO_ISPB,
                branch = "123",
                accountNumber = "123",
                accountType = AccountType.CACC
            ),
            owner = Owner(type = TypeOwner.NATURAL_PERSON, name = "Rodrigo", taxIdNumber = "99999999999")
        )

        val responseBcb = CreatePixKeyResponse(
            keyType = KeyType.RANDOM, key = "bc35591d-b547-4151-a325-4a9d2cd19614",
            bankAccount = BankAccount(
                participant = DadosBancarios.ITAU_UNIBANCO_ISPB,
                branch = "123",
                accountNumber = "123",
                accountType = AccountType.CACC
            ),
            owner = Owner(type = TypeOwner.NATURAL_PERSON, name = "Rodrigo", taxIdNumber = "99999999999"),
            createdAt = LocalDateTime.now()
        )

        Mockito.`when`(
            bcbClient.cadastra(requestBcb)
        ).thenReturn(HttpResponse.created(responseBcb))

        Mockito.`when`(
            itauClient.buscaContaPorClienteETipo(
                request.identificadorCliente,
                request.tipoContaMessage.name
            )
        ).thenReturn(HttpResponse.ok(responseItauClient))

        //Ação
        val erro = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.registraChave(request)
        }

        //Verificação
        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)

    }

    @Test
    fun `deve gerar uma exception ao nao encontrar cliente especificado no banco`() {
        //Cenário
        repository.deleteAll()
        val request = RegistraChaveRequest.newBuilder()
            .setIdentificadorCliente("bc35591d-b547-4151-a325-4a9d2cd19614")
            .setTipoChaveMessage(TipoChaveMessage.EMAIL)
            .setTipoContaMessage(TipoContaMessage.CONTA_CORRENTE)
            .setValorDaChave("mentor@zup.com.br")
            .build()

        val responseItauClient = ContasItauResponse(
            TipoConta.CONTA_CORRENTE, "123", "123",
            InstituicaoResponse("Itau", "789"),
            TitularResponse(UUID.randomUUID(), "Rodrigo", "99999999999")
        )

        val requestBcb = CreatePixKeyRequest(
            keyType = KeyType.RANDOM, key = "",
            bankAccount = BankAccount(
                participant = DadosBancarios.ITAU_UNIBANCO_ISPB,
                branch = "123",
                accountNumber = "123",
                accountType = AccountType.CACC
            ),
            owner = Owner(type = TypeOwner.NATURAL_PERSON, name = "Rodrigo", taxIdNumber = "99999999999")
        )

        val responseBcb = CreatePixKeyResponse(
            keyType = KeyType.RANDOM, key = "bc35591d-b547-4151-a325-4a9d2cd19614",
            bankAccount = BankAccount(
                participant = DadosBancarios.ITAU_UNIBANCO_ISPB,
                branch = "123",
                accountNumber = "123",
                accountType = AccountType.CACC
            ),
            owner = Owner(type = TypeOwner.NATURAL_PERSON, name = "Rodrigo", taxIdNumber = "99999999999"),
            createdAt = LocalDateTime.now()
        )

        Mockito.`when`(
            bcbClient.cadastra(requestBcb)
        ).thenReturn(HttpResponse.created(responseBcb))


        Mockito.`when`(
            itauClient.buscaContaPorClienteETipo(
                request.identificadorCliente,
                request.tipoContaMessage.name
            )
        ).thenReturn(HttpResponse.badRequest())

        //Ação
        val erro = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.registraChave(request)
        }

        //Verificação
        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)

    }

    @Test
    fun `deve gerar uma exception ao cadastar chave repetida`() {
        //Cenário
        repository.deleteAll()
        val dadosBancarios = DadosBancarios("1234", "1234", "Itau", "Rodrigo", "12345467891")
        val chave = ChavePix(
            identificadorCliente = UUID.randomUUID(),
            tipoChave = TipoChave.EMAIL,
            tipoConta = TipoConta.CONTA_CORRENTE,
            chave = "mentor@zup.com.br",
            dadosBancario = dadosBancarios
        )
        repository.save(chave)

        val request = RegistraChaveRequest.newBuilder()
            .setIdentificadorCliente(chave.identificadorCliente.toString())
            .setTipoChaveMessage(TipoChaveMessage.valueOf(chave.tipoChave.name))
            .setTipoContaMessage(TipoContaMessage.valueOf(chave.tipoConta.name))
            .setValorDaChave(chave.chave)
            .build()

        val responseItauClient = ContasItauResponse(
            TipoConta.CONTA_CORRENTE, "123", "123",
            InstituicaoResponse("Itau", "789"),
            TitularResponse(UUID.randomUUID(), "Rodrigo", "99999999999")
        )

        val requestBcb = CreatePixKeyRequest(
            keyType = KeyType.RANDOM, key = "",
            bankAccount = BankAccount(
                participant = DadosBancarios.ITAU_UNIBANCO_ISPB,
                branch = "123",
                accountNumber = "123",
                accountType = AccountType.CACC
            ),
            owner = Owner(type = TypeOwner.NATURAL_PERSON, name = "Rodrigo", taxIdNumber = "99999999999")
        )

        val responseBcb = CreatePixKeyResponse(
            keyType = KeyType.RANDOM, key = "bc35591d-b547-4151-a325-4a9d2cd19614",
            bankAccount = BankAccount(
                participant = DadosBancarios.ITAU_UNIBANCO_ISPB,
                branch = "123",
                accountNumber = "123",
                accountType = AccountType.CACC
            ),
            owner = Owner(type = TypeOwner.NATURAL_PERSON, name = "Rodrigo", taxIdNumber = "99999999999"),
            createdAt = LocalDateTime.now()
        )

        Mockito.`when`(
            bcbClient.cadastra(requestBcb)
        ).thenReturn(HttpResponse.created(responseBcb))


        Mockito.`when`(
            itauClient.buscaContaPorClienteETipo(
                request.identificadorCliente,
                request.tipoContaMessage.name
            )
        ).thenReturn(HttpResponse.ok(responseItauClient))

        //Ação
        val erro = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.registraChave(request)
        }

        //Verificação
        assertEquals(Status.ALREADY_EXISTS.code, erro.status.code)

    }

    @Test
    fun `deve gerar uma exception ao informar id de cliente sem ser uuid`() {
        //Cenário
        repository.deleteAll()

        val request = RegistraChaveRequest.newBuilder()
            .setIdentificadorCliente("bc35591d-b547")
            .setTipoChaveMessage(TipoChaveMessage.ALEATORIA)
            .setTipoContaMessage(TipoContaMessage.CONTA_CORRENTE)
            .build()

        val responseItauClient = ContasItauResponse(
            TipoConta.CONTA_CORRENTE, "123", "123",
            InstituicaoResponse("Itau", "789"),
            TitularResponse(UUID.randomUUID(), "Rodrigo", "99999999999")
        )

        val requestBcb = CreatePixKeyRequest(
            keyType = KeyType.RANDOM, key = "",
            bankAccount = BankAccount(
                participant = DadosBancarios.ITAU_UNIBANCO_ISPB,
                branch = "123",
                accountNumber = "123",
                accountType = AccountType.CACC
            ),
            owner = Owner(type = TypeOwner.NATURAL_PERSON, name = "Rodrigo", taxIdNumber = "99999999999")
        )

        val responseBcb = CreatePixKeyResponse(
            keyType = KeyType.RANDOM, key = "bc35591d-b547-4151-a325-4a9d2cd19614",
            bankAccount = BankAccount(
                participant = DadosBancarios.ITAU_UNIBANCO_ISPB,
                branch = "123",
                accountNumber = "123",
                accountType = AccountType.CACC
            ),
            owner = Owner(type = TypeOwner.NATURAL_PERSON, name = "Rodrigo", taxIdNumber = "99999999999"),
            createdAt = LocalDateTime.now()
        )

        Mockito.`when`(
            bcbClient.cadastra(requestBcb)
        ).thenReturn(HttpResponse.created(responseBcb))


        Mockito.`when`(
            itauClient.buscaContaPorClienteETipo(
                request.identificadorCliente,
                request.tipoContaMessage.name
            )
        ).thenReturn(HttpResponse.ok(responseItauClient))

        //Ação
        val erro = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.registraChave(request)
        }

        //Verificação
        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)

    }


    @MockBean(ContasItauClient::class)
    fun mockContasItauClient(): ContasItauClient {
        return Mockito.mock(ContasItauClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun mockBcbClient(): BcbClient {
        return Mockito.mock((BcbClient::class.java))
    }



}