package br.com.rodrigo.pix.consulta

import br.com.rodrigo.pix.ChavePix
import br.com.rodrigo.pix.DadosBancarios
import br.com.rodrigo.pix.TipoChave
import br.com.rodrigo.pix.TipoConta
import java.time.LocalDateTime
import java.util.*

data class ChavePixInfo(
    val pixId: UUID? = null,
    val clienteId: UUID? = null,
    val tipoChave: TipoChave,
    val chave: String,
    val tipoConta: TipoConta,
    val dadosBancarios: DadosBancarios,
    val registradaEm: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun build(chave: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                pixId = chave.id,
                clienteId = chave.identificadorCliente,
                tipoChave = chave.tipoChave,
                chave = chave.chave,
                tipoConta = chave.tipoConta,
                dadosBancarios = chave.dadosBancario,
                registradaEm = chave.criadaEm
            )
        }
    }
}