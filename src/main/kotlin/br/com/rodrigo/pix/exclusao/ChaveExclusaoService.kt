package br.com.rodrigo.pix.exclusao

import br.com.rodrigo.integracao.bcb.BcbClient
import br.com.rodrigo.integracao.bcb.classes.DeletePixKeyRequest
import br.com.rodrigo.integracao.itau.ContasItauClient
import br.com.rodrigo.pix.ChavePixRepository
import br.com.rodrigo.pix.DadosBancarios
import br.com.rodrigo.util.exception.ChavePixInexistenteException
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class ChaveExclusaoService(
    val repository: ChavePixRepository,
    val bcbClient: BcbClient
) {

    fun removeChavePix(@Valid exclusaoChaveDto: ExclusaoChaveDto): String {

        //Verifica se a chave existe
        val possivelChave = repository.findById(UUID.fromString(exclusaoChaveDto.chaveId))
        if (possivelChave.isEmpty) throw ChavePixInexistenteException("A chave ${exclusaoChaveDto.chaveId} não foi encontrada!")

        //Verifica se o clienteId informado é dono da chave, só se for que poderá deletar
        if (possivelChave.get().verificaSeEhDono(UUID.fromString(exclusaoChaveDto.clienteId))) {
            //Deletando chave no BCB
            val deletaResponse = bcbClient.deleta(possivelChave.get().chave, DeletePixKeyRequest(possivelChave.get().chave, DadosBancarios.ITAU_UNIBANCO_ISPB))

            //Verifica se conseguiu deletar a chave no BCB, se sim, podemos deletá-la do nosso Sistema.
            if (deletaResponse.code() == 200) {
                repository.delete(possivelChave.get())
            }else{
                throw IllegalStateException("Erro ao apagar chave Pix no Banco Central do Brasil (BCB)")
            }

        } else {
            throw IllegalAccessException("Não foi permitido fazer sua operação.")
        }

        return exclusaoChaveDto.chaveId
    }
}