package br.com.zup.orangetalents

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test

@MicronautTest
internal class ChavePixEndpointTest{

    @Test
    fun `deve adicionar chave`(){

    }
}

private fun dadosDaContaResponse(): SistemaItauResponse {
    return SistemaItauResponse(
        tipo = "CONTA_CORRENTE",
        instituicao = SistemaItauInstituicao(nome = "ITAÚ UNIBANCO S.A.", ispb = "60701190"),
        agencia = "0001",
        numero = "291900",
        titular = SistemaItauTitular(
            id = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            nome = "Rafael M C Ponte",
            cpf = "02467781054"
        )
    )
}