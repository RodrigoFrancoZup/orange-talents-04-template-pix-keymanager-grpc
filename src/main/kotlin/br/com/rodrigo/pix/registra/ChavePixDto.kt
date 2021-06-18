package br.com.rodrigo.pix.registra

import br.com.rodrigo.pix.ChavePix
import br.com.rodrigo.pix.DadosBancarios
import br.com.rodrigo.pix.TipoChave
import br.com.rodrigo.pix.TipoConta
import br.com.rodrigo.util.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class ChavePixDto(
    @field:ValidUUID
    @field:NotBlank
    val clienteId: String,

    @field:NotNull
    val tipo: TipoChave?,

    @field:Size(max = 77)
    val chave: String,

    @field:NotNull
    val tipoConta: TipoConta?
){

    fun toModel(dadosBancarios: DadosBancarios): ChavePix {
        return ChavePix(
            identificadorCliente = UUID.fromString(clienteId),
            tipoChave = tipo!!,
            tipoConta = tipoConta!!,
            chave = if(tipo == TipoChave.ALEATORIA) UUID.randomUUID().toString() else this.chave,
            dadosBancario = dadosBancarios
        )
    }
}
