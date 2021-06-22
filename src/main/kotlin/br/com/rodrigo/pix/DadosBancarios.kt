package br.com.rodrigo.pix


import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Embeddable
class DadosBancarios(

    @field:NotBlank
    @field:Size(max = 4)
    @Column(name = "conta_agencia", length = 4, nullable = false)
    val agencia: String,

    @field:NotBlank
    @field:Size(max = 6)
    @Column(name = "conta_numero", nullable = false)
    val numero: String,

    @field:NotBlank
    @Column(name = "conta_instituicao")
    val instituicao: String,

    @field:NotBlank@Column(name = "conta_titular")
    val titularNome: String,

    @field:NotBlank
    @field:Size(max = 11)
    @Column(name = "conta_titular_cpf")
    val cpf: String
) {

    companion object {
         val ITAU_UNIBANCO_ISPB: String = "60701190"
    }
}