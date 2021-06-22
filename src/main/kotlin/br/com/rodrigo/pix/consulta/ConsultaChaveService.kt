package br.com.rodrigo.pix.consulta

import br.com.rodrigo.integracao.bcb.BcbClient
import br.com.rodrigo.pix.ChavePix
import br.com.rodrigo.pix.ChavePixRepository
import br.com.rodrigo.util.exception.ChavePixInexistenteException
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Singleton
import javax.validation.Valid

/**
 * Essa classe não está mais em uso!
 * Adotei a abordagem do mentor, de utilizar Oneof no arquivo .proto e uma classe sealed, ao invés
 * de utilizar dois endpoint (um para cada tipo de consulta)
 */
@Validated
@Singleton
class ConsultaChaveService(
    val repository: ChavePixRepository,
    val bcbClient: BcbClient
) {

    fun consulta(@Valid consultaChaveDto: ConsultaChaveDto): ChavePix {

       val possivelChave =  repository.findByIdAndIdentificadorCliente(UUID.fromString(consultaChaveDto.chaveId),UUID.fromString(consultaChaveDto.clienteId))
        if(possivelChave.isPresent){
            val pixKeyDetailsResponse = bcbClient.busca(possivelChave.get().chave)
            if(pixKeyDetailsResponse.status.code == 200){
                return possivelChave.get()
            }else{
                throw ChavePixInexistenteException("Chave pix não cadastrada no BCB!")
            }
        }else{
            throw ChavePixInexistenteException("Chave pix não cadastrada no Itau!")
        }
    }
}