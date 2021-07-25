package br.com.zup.orangetalents.commons.external.bcb

import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class DetalhesChaveBCBResponse(
    @field:NotBlank val keyType: String,
    @field:NotBlank val key: String,
    @field:NotNull val bankAccount: BCBBankAccountResponse,
    @field:NotNull val owner: BCBOwnerResponse,
    val createdAt: LocalDateTime
)

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

