package br.com.rodrigo.pix.consulta

import br.com.rodrigo.util.validation.ValidUUID
import javax.validation.constraints.NotBlank

data class ConsultaChaveDto(

    @NotBlank
    @ValidUUID
    val clienteId: String,

    @NotBlank
    @ValidUUID
    val chaveId: String
) {
}