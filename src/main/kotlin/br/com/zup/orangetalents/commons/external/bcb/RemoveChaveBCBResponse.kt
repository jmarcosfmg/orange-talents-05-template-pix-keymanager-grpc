package br.com.zup.orangetalents.commons.external.bcb

import com.google.type.DateTime

abstract class RemoveChaveBCBResponse

data class RemoveChaveBCBOkResponse(
    val key: String,
    val participant: String,
    val deletedAt: DateTime
) : RemoveChaveBCBResponse()

data class RemoveChaveBCBProblemResponse(
    val type: String,
    val status: Int,
    val title: String,
    val detail: String,
    val violations: Array<Violations>
) : RemoveChaveBCBResponse()

data class Violations(
    val field: String,
    val message: String
)