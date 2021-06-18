package br.com.rodrigo.integracao.bcb.classes

import br.com.rodrigo.pix.ChavePix
import br.com.rodrigo.pix.DadosBancarios
import br.com.rodrigo.pix.TipoChave
import br.com.rodrigo.pix.TipoConta
import java.lang.RuntimeException

data class CreatePixKeyRequest(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
) {

    companion object {

        fun build(chave: ChavePix): CreatePixKeyRequest {

            val accountType: AccountType
            val keyType: KeyType

            when (chave.tipoConta) {
                TipoConta.CONTA_CORRENTE -> accountType = AccountType.CACC
                else -> accountType = AccountType.SVGS
            }

            when (chave.tipoChave) {
                TipoChave.EMAIL -> keyType = KeyType.EMAIL
                TipoChave.CPF -> keyType = KeyType.CPF
                TipoChave.CELULAR -> keyType = KeyType.PHONE
                TipoChave.ALEATORIA -> keyType = KeyType.RANDOM
                else -> throw RuntimeException("Falha ao converter TipoChave para KeyType")
            }

            return CreatePixKeyRequest(
                key = chave.chave,
                bankAccount = BankAccount(
                    participant = DadosBancarios.ITAU_UNIBANCO_ISPB,
                    branch = chave.dadosBancario.agencia, accountNumber = chave.dadosBancario.numero,
                    accountType = accountType
                ),
                owner = Owner(
                    type = TypeOwner.NATURAL_PERSON,
                    name = chave.dadosBancario.titularNome,
                    taxIdNumber = chave.dadosBancario.cpf
                ),
                keyType = keyType
            )
        }
    }
}