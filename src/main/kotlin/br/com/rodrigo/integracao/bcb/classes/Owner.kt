package br.com.rodrigo.integracao.bcb.classes

data class Owner(
    val type: TypeOwner,
    val name: String,
    val taxIdNumber: String
) {
}