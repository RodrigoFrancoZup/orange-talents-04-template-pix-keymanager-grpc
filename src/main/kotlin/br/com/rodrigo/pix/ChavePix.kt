package br.com.rodrigo.pix

import java.util.*
import javax.persistence.*

@Entity
class ChavePix(

    val identificadorCliente: UUID,

    @Enumerated(EnumType.STRING)
    val tipoChave: TipoChave,

    @Enumerated(EnumType.STRING)
    val tipoConta: TipoConta,
    val chave: String
) {

    @Id
    @GeneratedValue
    val id: UUID? = null
}


