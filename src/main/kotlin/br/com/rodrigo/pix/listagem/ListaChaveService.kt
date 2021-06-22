package br.com.rodrigo.pix.listagem

import br.com.rodrigo.pix.ChavePix
import br.com.rodrigo.pix.ChavePixRepository
import br.com.rodrigo.util.validation.ValidUUID
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class ListaChaveService(
    @Inject val repository: ChavePixRepository
) {

    fun listaChave(@NotBlank @ValidUUID clientId: String): List<ChavePix> {

        return repository.findByIdentificadorCliente(UUID.fromString(clientId))
    }
}