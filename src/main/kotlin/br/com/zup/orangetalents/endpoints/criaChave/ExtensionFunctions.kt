package br.com.zup.orangetalents.endpoints.criaChave

import br.com.zup.orangetalents.commons.exceptions.ChaveExistsViolationException
import br.com.zup.orangetalents.commons.exceptions.ContaNotFoundViolationException
import br.com.zup.orangetalents.commons.exceptions.ServerCommunicationException
import br.com.zup.orangetalents.commons.external.SistemaItau
import br.com.zup.orangetalents.model.ChavePix
import br.com.zup.orangetalents.repositories.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.exceptions.HttpStatusException


fun SistemaItau.contaExists(cliente: String, tipoConta: String): Boolean {
    try {
        val response = this.buscaPorClienteETipoConta(cliente, "CONTA_${tipoConta}")
            .takeIf { it.status.code != HttpStatus.INTERNAL_SERVER_ERROR.code }
            ?: throw ContaNotFoundViolationException(
                HttpStatusException(HttpStatus.BAD_REQUEST, null)
            )
    } catch (e: HttpClientException) {
        throw ServerCommunicationException(e)
    }
    return true
}

fun ChavePixRepository.insertIfNotExists(chave: ChavePix): ChavePix {
    return this.let {
        chave.takeIf {
            this.buscaPorChaveETipo(it.chave, it.tipoChave)
                .isEmpty()
        }?.run { it.save(this) }
            ?: throw ChaveExistsViolationException("Chave já registrada")
    }
}