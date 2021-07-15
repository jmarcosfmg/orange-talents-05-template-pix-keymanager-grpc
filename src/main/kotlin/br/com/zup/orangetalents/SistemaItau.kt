package br.com.zup.orangetalents

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${sistemaExternos.contasItau}")
interface SistemaItau {

    @Get("/{clienteId}/contas")
    fun buscaDadosCliente(@PathVariable clienteId: String,
                            @QueryValue tipo: String): SistemaItauResponse?

}