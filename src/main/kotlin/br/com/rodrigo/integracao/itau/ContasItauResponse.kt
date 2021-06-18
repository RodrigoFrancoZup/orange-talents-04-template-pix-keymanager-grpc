package br.com.rodrigo.integracao.itau

import br.com.rodrigo.pix.DadosBancarios
import br.com.rodrigo.pix.TipoConta
import java.util.*
import javax.persistence.Column
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class ContasItauResponse(
    val tipo: TipoConta,
    val agencia: String,
    val numero: String,
    val instituicao: InstituicaoResponse,
    val titular: TitularResponse
) {

    fun toDadosBancarios(): DadosBancarios {
        return DadosBancarios(agencia, numero, instituicao.nome, titular.nome, titular.cpf)
    }

}

data class InstituicaoResponse(val nome: String, val ispb: String)
data class TitularResponse(val id: UUID, val nome: String, val cpf: String)

