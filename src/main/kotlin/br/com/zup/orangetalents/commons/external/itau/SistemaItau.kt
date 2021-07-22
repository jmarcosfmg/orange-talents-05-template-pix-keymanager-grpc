package br.com.zup.orangetalents.commons.external.itau

import br.com.zup.orangetalents.ClienteETipoContaResponse
import br.com.zup.orangetalents.commons.exceptions.ContaNotFoundViolationException
import br.com.zup.orangetalents.commons.exceptions.ServerCommunicationException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.exceptions.HttpStatusException
import javax.validation.constraints.NotBlank

@Client("\${sistemaExternos.contasItau}")
interface SistemaItau {

    @Get("/{clienteId}/contas")
    fun buscaContaCliente(
        @PathVariable clienteId: String,
        @QueryValue tipo: String
    ): HttpResponse<ClienteETipoContaResponse>

    @Get("/{clienteId}")
    fun buscaPorCliente(
        @PathVariable clienteId: String,
    ): HttpResponse<ClienteResponse>
}

fun SistemaItau.buscaPorClienteETipoConta(
    @NotBlank clienteId: String,
    @NotBlank tipo: String
): ClienteETipoContaResponse {
    val cliente: HttpResponse<ClienteETipoContaResponse> = this.buscaContaCliente(clienteId, "CONTA_${tipo}").apply {
        if (this.status.code == HttpStatus.INTERNAL_SERVER_ERROR.code)
            throw ContaNotFoundViolationException(HttpStatusException(HttpStatus.BAD_REQUEST, null))
    }
    return try {
        cliente.body()!!
    } catch (e: Exception) {
        throw ServerCommunicationException(e)
    }
}