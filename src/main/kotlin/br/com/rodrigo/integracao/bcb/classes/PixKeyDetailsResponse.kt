package br.com.rodrigo.integracao.bcb.classes

import br.com.rodrigo.pix.ChavePix
import br.com.rodrigo.pix.DadosBancarios
import br.com.rodrigo.pix.TipoChave
import br.com.rodrigo.pix.TipoConta
import br.com.rodrigo.pix.consulta.ChavePixInfo
import java.lang.RuntimeException
import java.time.LocalDateTime

data class PixKeyDetailsResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {

    fun convertToChavePixInfo(): ChavePixInfo {
        val tipoChave: TipoChave
        val tipoConta: TipoConta
        val dadosBancarios = DadosBancarios(bankAccount.branch,bankAccount.accountNumber,
        bankAccount.participant, owner.name, owner.taxIdNumber)

        when(keyType){
            KeyType.CPF -> tipoChave = TipoChave.CPF
            KeyType.PHONE -> tipoChave = TipoChave.CELULAR
            KeyType.EMAIL -> tipoChave = TipoChave.EMAIL
            KeyType.RANDOM -> tipoChave = TipoChave.ALEATORIA
            else -> throw RuntimeException("Falha ao converter PixKeyDetailsResponse para ChavePixInfo")
        }

        when(bankAccount.accountType){
            AccountType.CACC -> tipoConta = TipoConta.CONTA_CORRENTE
            else -> tipoConta = TipoConta.CONTA_POUPANCA
        }
        return ChavePixInfo(
            tipoChave = tipoChave,
            registradaEm = createdAt,
            tipoConta = tipoConta,
            chave = key,
            dadosBancarios = dadosBancarios
        )

    }

}