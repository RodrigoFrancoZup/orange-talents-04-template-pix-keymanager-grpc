package br.com.rodrigo.pix.exclusao

import br.com.rodrigo.util.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class ExclusaoChaveDto(

    @field:ValidUUID
    @field:NotBlank
    val clienteId: String,

    @field:ValidUUID
    @field:NotBlank
    val chaveId: String
) {
}

