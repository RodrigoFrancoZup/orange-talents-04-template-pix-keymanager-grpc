package br.com.rodrigo.pix.registra

import br.com.rodrigo.integracao.itau.ContasItauClient
import br.com.rodrigo.pix.ChavePix
import br.com.rodrigo.pix.ChavePixRepository
import br.com.rodrigo.util.exception.ChavePixExistenteException
import br.com.rodrigo.util.handler.ExceptionHandler
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class ChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val client: ContasItauClient
) {

    fun registra(@Valid chavePixDto: ChavePixDto):ChavePix{

        var chavePix: ChavePix? = null

        // Verifica se chave já existe/já é usada
        if(repository.existsByChave(chavePixDto.chave)){
            throw  ChavePixExistenteException("A chave ${chavePixDto.chave} já está cadastrada!")
        }

        //Consultando o serviço do Itau para ver se o clienteId informado tem de fato a conta
        val contaItauResponse = client.buscaContaPorClienteETipo(chavePixDto.clienteId, chavePixDto.tipoConta.toString())

        //Se a consulta ao serviço Itau confirmou que há conta, podemos salvar
        if(contaItauResponse.body.isPresent){
            chavePix = chavePixDto.toModel()
            repository.save(chavePix)
        }else{
            throw IllegalStateException("Cliente não encontrado no Itau")
        }

        return chavePix!!
    }
}