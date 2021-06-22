package br.com.rodrigo.pix.consulta

import br.com.rodrigo.integracao.bcb.BcbClient
import br.com.rodrigo.pix.ChavePix
import br.com.rodrigo.pix.ChavePixRepository
import br.com.rodrigo.util.exception.ChavePixInexistenteException
import br.com.rodrigo.util.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    /**
     * Deve retornar chave encontrada ou lançar um exceção de erro de chave não encontrada
     */
    abstract fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo

    @Introspected
    data class PorFiltroIds(
        @field:NotBlank @field:ValidUUID val clienteId: String,
        @field:NotBlank @field:ValidUUID val pixId: String,
    ) : Filtro() {

        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo {
            val possivelChavePix =
                repository.findByIdAndIdentificadorCliente(UUID.fromString(pixId), UUID.fromString(clienteId))
            if (possivelChavePix.isPresent) {
                return ChavePixInfo.build(possivelChavePix.get())
            } else {
                throw ChavePixInexistenteException("Chave Pix não encontrada")
            }
        }
    }

    @Introspected
    data class PorChave(@field:NotBlank @Size(max = 77) val chave: String) : Filtro() {

        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo {
            val possivelChave = repository.findByChave(chave)
            if (possivelChave.isPresent) {
                return ChavePixInfo.build(possivelChave.get())
            }
            val responseBcb = bcbClient.busca(chave)
            if(responseBcb.status.code == 200){
                return responseBcb.body().convertToChavePixInfo()
            }
            throw ChavePixInexistenteException("Chave Pix não encontrada")
        }
    }

    @Introspected
    class Invalido() : Filtro() {
        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo {
            throw IllegalArgumentException("Chave Pix inválida ou não informada")
        }
    }
}
