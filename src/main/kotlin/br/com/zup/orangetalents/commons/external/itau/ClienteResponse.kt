package br.com.zup.orangetalents.commons.external.itau

data class ClienteResponse(
    val id: String,
    val nome: String,
    val cpf: String,
    val instituicao: SistemaItauInstituicao
) {

}
