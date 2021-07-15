package br.com.zup.orangetalents

data class SistemaItauResponse(
    val tipo: String,
    val instituicao: SistemaItauInstituicao,
    val agencia: String,
    val numero: String,
    val titular: SistemaItauTitular
)

data class SistemaItauInstituicao(val nome: String, val ispb: String)

data class SistemaItauTitular(val id: String, val nome: String, val cpf: String)