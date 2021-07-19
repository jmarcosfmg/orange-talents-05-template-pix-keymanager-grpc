package br.com.zup.orangetalents

import br.com.zup.orangetalents.commons.external.SistemaItauInstituicao

data class ClienteETipoContaResponse(
    val tipo: String,
    val instituicao: SistemaItauInstituicao,
    val agencia: String,
    val numero: String,
    val titular: SistemaItauTitular
)

data class SistemaItauTitular(val id: String, val nome: String, val cpf: String)