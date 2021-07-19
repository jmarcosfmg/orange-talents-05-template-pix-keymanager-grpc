package br.com.zup.orangetalents.commons.external

data class ClienteResponse(
    val id: String,
    val nome: String,
    val cpf: String,
    val instituicao: SistemaItauInstituicao
) {

}
