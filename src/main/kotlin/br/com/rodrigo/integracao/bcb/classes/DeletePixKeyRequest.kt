package br.com.rodrigo.integracao.bcb.classes

data class DeletePixKeyRequest(
    val key: String,
    val participant: String
) {
}