package br.com.rodrigo.pix

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class ChavePixTest{

    @Test
    fun `deve retonrar true quando o cliente for dono da chave`(){

        //Cenario
        val clienteId = UUID.randomUUID()
        val dadosBancarios = DadosBancarios("1234", "1234","Itau","Rodrigo","12345467891")
        val chavePix = ChavePix(clienteId, TipoChave.EMAIL, TipoConta.CONTA_CORRENTE, "rodrigo@gmail.com", dadosBancarios)

        //Ação
        val resultadoVerificacao = chavePix.verificaSeEhDono(clienteId)

        //Verificação
        assertTrue(resultadoVerificacao)

    }

    @Test
    fun `deve retonrar false quando o cliente nao for dono da chave`(){

        //Cenario
        val clienteId = UUID.randomUUID()
        val outroDono = UUID.randomUUID()
        val dadosBancarios = DadosBancarios("1234", "1234","Itau","Rodrigo","12345467891")
        val chavePix = ChavePix(clienteId, TipoChave.EMAIL, TipoConta.CONTA_CORRENTE, "rodrigo@gmail.com", dadosBancarios)

        //Ação
        val resultadoVerificacao = chavePix.verificaSeEhDono(outroDono)

        //Verificação
        assertFalse(resultadoVerificacao)

    }

}