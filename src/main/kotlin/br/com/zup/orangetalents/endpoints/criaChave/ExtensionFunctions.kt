package br.com.zup.orangetalents.endpoints.criaChave

import br.com.zup.orangetalents.ChavePixRequest
import br.com.zup.orangetalents.SistemaItau
import br.com.zup.orangetalents.commons.exceptions.ContaNotFoundViolationException


fun SistemaItau.contaExists(request: ChavePixRequest): Boolean {
    try {
        this.buscaDadosCliente(request.codigo, request.tipoConta.name)
    } catch (e: Exception) {
        throw ContaNotFoundViolationException(e)
    }
    return true
}