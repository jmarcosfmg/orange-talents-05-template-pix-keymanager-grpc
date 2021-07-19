package br.com.zup.orangetalents.commons.external

import br.com.zup.orangetalents.ClienteETipoContaResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${sistemaExternos.contasItau}")
interface SistemaItau {

    @Get("/{clienteId}/contas")
    fun buscaPorClienteETipoConta(
        @PathVariable clienteId: String,
        @QueryValue tipo: String
    ): HttpResponse<ClienteETipoContaResponse>

    @Get("/{clienteId}")
    fun buscaPorCliente(
        @PathVariable clienteId: String,
    ): HttpResponse<ClienteResponse>


}