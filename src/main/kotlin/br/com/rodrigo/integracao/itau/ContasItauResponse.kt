package br.com.rodrigo.integracao.itau

import br.com.rodrigo.pix.TipoConta
import java.util.*

data class ContasItauResponse(
    val tipo: TipoConta,
    val agencia: String,
    val numero: String,
    val instituicao: InstituicaoResponse,
    val titular: TitularResponse
)

data class InstituicaoResponse(val nome: String, val ispb: String)
data class TitularResponse(val id: UUID, val nome: String, val cpf: String)