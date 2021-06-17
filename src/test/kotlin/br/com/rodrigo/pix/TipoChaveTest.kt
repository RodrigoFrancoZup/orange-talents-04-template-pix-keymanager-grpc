package br.com.rodrigo.pix

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


internal class TipoChaveTest() {

    //Teste Email
    @Test
    fun `deve aceitar a chave email com o valor correto`() {

        val tipoChave = TipoChave.EMAIL
        val validacao = tipoChave.valida("rodrigo@email.com.br")
        assertTrue(validacao)

    }

    @Test
    fun `deve aceitar a chave email com o valor correto teste de outra forma`() {

        with(TipoChave.EMAIL) {
            assertTrue(valida("rodrigo@email.com.br"))
        }

    }

    @Test
    fun `deve recusar a chave email sem valor`() {
        with(TipoChave.EMAIL) {
            assertFalse((valida(null)))
        }
    }

    @Test
    fun `deve recusar a chave email com valor vazio`() {
        with(TipoChave.EMAIL) {
            assertFalse(valida(""))
        }
    }

    @Test
    fun `deve recusar a chave email com valor incorreto`() {
        with(TipoChave.EMAIL) {
            assertFalse(valida("rodrigo.com"))
        }
    }

    //Teste CPF
    @Test
    fun `deve aceitar a chave cpf com o valor correto`() {

        with(TipoChave.CPF) {
            assertTrue(valida("412.192.000-77"))
        }

    }

    @Test
    fun `deve recusar a chave cpf sem valor`() {
        with(TipoChave.CPF) {
            assertFalse((valida(null)))
        }
    }

    @Test
    fun `deve recusar a chave cpf  com valor vazio`() {
        with(TipoChave.CPF) {
            assertFalse(valida(""))
        }
    }

    @Test
    fun `deve recusar a chave cpf com valor incorreto`() {
        with(TipoChave.CPF) {
            assertFalse(valida("123"))
        }
    }

    //Teste CELULAR
    @Test
    fun `deve aceitar a chave celular com o valor correto`() {

        with(TipoChave.CELULAR) {
            assertTrue(valida("+553599999999"))
        }

    }

    @Test
    fun `deve recusar a chave celular sem valor`() {
        with(TipoChave.CELULAR) {
            assertFalse((valida(null)))
        }
    }

    @Test
    fun `deve recusar a chave celular  com valor vazio`() {
        with(TipoChave.CELULAR) {
            assertFalse(valida(""))
        }
    }

    @Test
    fun `deve recusar a chave celular com valor incorreto`() {
        with(TipoChave.CELULAR) {
            assertFalse(valida("123"))
        }
    }

    //Teste Aleatoria
    @Test
    fun `deve aceitar a chave aleatoria com o valor correto`(){
        with(TipoChave.ALEATORIA){
            assertTrue(valida(null))
        }
    }

    @Test
    fun `deve recusar a chave aleatoria com valor incorreto`() {
        with(TipoChave.ALEATORIA) {
            assertFalse(valida("123"))
        }
    }
}

