package br.com.zup.orangetalents.commons.external.bcb

import com.google.type.DateTime

class CriaChaveBCBResponse(
    val keyType: String,
    val key: String,
    val bankAccount: BCBBankAccountResponse,
    val owner: BCBOwnerResponse,
    val createdAt: DateTime
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CriaChaveBCBResponse

        if (keyType != other.keyType) return false
        if (key != other.key) return false
        if (bankAccount != other.bankAccount) return false
        if (owner != other.owner) return false

        return true
    }
}

data class BCBBankAccountResponse(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: String
)

data class BCBOwnerResponse(
    val type: String,
    val name: String,
    val taxIdNumber: String
)

