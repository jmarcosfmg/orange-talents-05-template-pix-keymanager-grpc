package br.com.zup.orangetalents.commons.external.bcb

import java.time.LocalDateTime

abstract class RemoveChaveBCBResponse

data class RemoveChaveBCBOkResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
) : RemoveChaveBCBResponse()
