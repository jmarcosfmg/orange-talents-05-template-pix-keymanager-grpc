package br.com.zup.orangetalents.endpoints.criaChave

import br.com.zup.orangetalents.ChavePixRequest
import br.com.zup.orangetalents.SistemaItauResponse
import br.com.zup.orangetalents.commons.exceptions.ContaNotFoundViolationException
import br.com.zup.orangetalents.commons.exceptions.ServerCommunicationException
import br.com.zup.orangetalents.commons.external.SistemaItau
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.exceptions.HttpStatusException


fun SistemaItau.contaExists(request: ChavePixRequest): Boolean {
    try{
        val response = this.buscaDadosCliente(request.codigo, "CONTA_${request.tipoConta.name}")
            .takeIf { it.status.code != HttpStatus.INTERNAL_SERVER_ERROR.code } ?: throw ContaNotFoundViolationException(
            HttpStatusException(HttpStatus.BAD_REQUEST, null)
        )
    }catch (e : HttpClientException){
        throw ServerCommunicationException(e)
    }
    return true
}