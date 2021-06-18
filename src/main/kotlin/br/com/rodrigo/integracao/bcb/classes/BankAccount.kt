package br.com.rodrigo.integracao.bcb.classes

data class BankAccount(val participant: String,
val branch: String,
val accountNumber: String,
val accountType: AccountType) {
}