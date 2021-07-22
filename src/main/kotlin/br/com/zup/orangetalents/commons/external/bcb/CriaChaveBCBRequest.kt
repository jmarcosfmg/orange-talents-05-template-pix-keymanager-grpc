package br.com.zup.orangetalents.commons.external.bcb

import br.com.zup.orangetalents.ClienteETipoContaResponse
import br.com.zup.orangetalents.TipoChaveGrpc
import br.com.zup.orangetalents.commons.exceptions.ContaNotFoundViolationException
import br.com.zup.orangetalents.model.TipoChave
import br.com.zup.orangetalents.model.TipoConta
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException

data class CriaChaveBCBRequest(
    val keyType: BCBKeyType,
    val key: String,
    val bankAccount: BCBBankAccountRequest,
    val ownerRequest: BCBOwnerRequest
)

data class BCBOwnerRequest(
    val name: String,
    val taxIdNumber: String
){
    val type: String = "NATURAL_PERSON"
    constructor(cliente: ClienteETipoContaResponse) : this(
        name = cliente.titular.nome,
        taxIdNumber = cliente.titular.cpf
    )
}

data class BCBBankAccountRequest(
    val branch: String,
    val accountNumber: String,
    val accountType: String
) {
    val participant: String = "60701190"
}

enum class BCBAccountType(val tipoConta: TipoConta) {
    CACC(TipoConta.CORRENTE),
    SVGS(TipoConta.POUPANCA);

    companion object {
        fun fromTipoConta(tipoConta: TipoConta): BCBAccountType {
            return values().find {
                it.tipoConta.name.equals(tipoConta.name, ignoreCase = true)
            } ?: throw ContaNotFoundViolationException(
                HttpStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Tipo de Conta não encontrado"
                )
            )
        }
    }
}


enum class BCBKeyType(val tipoChave: TipoChave) {
    CPF(TipoChave.CPF),
    CNPJ(TipoChave.CPF),
    PHONE(TipoChave.CELULAR),
    EMAIL(TipoChave.EMAIL),
    RANDOM(TipoChave.ALEATORIA);

    companion object {
        fun fromTipoChaveGrpc(chaveGrpc: TipoChaveGrpc): BCBKeyType {
            return values().filter {
                it.tipoChave.name.equals(chaveGrpc.name, ignoreCase = true)
            }.firstOrNull() ?: throw HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Tipo de chave inválido")
        }
    }
}