package br.com.rodrigo.pix

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid

@Entity
class ChavePix(

    val identificadorCliente: UUID,

    @Enumerated(EnumType.STRING)
    val tipoChave: TipoChave,

    @Enumerated(EnumType.STRING)
    val tipoConta: TipoConta,
    var chave: String,

    @field:Valid
    @Embedded
    val dadosBancario: DadosBancarios
) {

    @Id
    @GeneratedValue
    val id: UUID? = null
    val criadaEm: LocalDateTime = LocalDateTime.now()

    fun verificaSeEhDono(clienteId: UUID) : Boolean{
        return this.identificadorCliente == clienteId
    }

    fun atualizaChaveAleatoriaBcb(chave: String){
        this.chave = chave
    }
}


