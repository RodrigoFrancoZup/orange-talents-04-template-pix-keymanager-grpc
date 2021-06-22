package br.com.rodrigo.pix.registra

import br.com.rodrigo.integracao.bcb.BcbClient
import br.com.rodrigo.integracao.bcb.classes.CreatePixKeyRequest
import br.com.rodrigo.integracao.bcb.classes.KeyType
import br.com.rodrigo.integracao.itau.ContasItauClient
import br.com.rodrigo.pix.ChavePix
import br.com.rodrigo.pix.ChavePixRepository
import br.com.rodrigo.util.exception.ChavePixExistenteException
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class ChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val client: ContasItauClient,
    @Inject val clientBcbClient: BcbClient
) {


    fun registra(@Valid chavePixDto: ChavePixDto): ChavePix {


        // Verifica se chave já existe/já é usada
        if (repository.existsByChave(chavePixDto.chave)) {
            throw  ChavePixExistenteException("A chave ${chavePixDto.chave} já está cadastrada!")
        }

        //Consultando o serviço do Itau para ver se o clienteId informado tem de fato a conta
        val contaItauResponse =
            client.buscaContaPorClienteETipo(chavePixDto.clienteId, chavePixDto.tipoConta.toString())

        //Se a consulta ao serviço Itau confirmou que há conta, podemos salvar
        if (contaItauResponse.body.isPresent) {
            val body = contaItauResponse.body
            val chavePix = chavePixDto.toModel(body.get().toDadosBancarios())

            //Cadastrando no BCB
            val createPixKeyRequest = CreatePixKeyRequest.build(chavePix)
            val responseBcb = clientBcbClient.cadastra(createPixKeyRequest)

            //Verifica se conseguiu salvar a chave no BCB
            if (responseBcb.code() == 201) {
                if (responseBcb.body()!!.keyType == KeyType.RANDOM) {
                    chavePix.atualizaChaveAleatoriaBcb(responseBcb.body()!!.key)
                }

                repository.save(chavePix)
            } else {
                throw IllegalStateException("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)")
            }
            return chavePix
        } else {
            throw IllegalStateException("Cliente não encontrado no Itau")
        }
    }
}