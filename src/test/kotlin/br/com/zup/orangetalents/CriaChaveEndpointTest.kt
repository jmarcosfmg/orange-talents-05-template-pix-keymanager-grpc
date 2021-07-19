package br.com.zup.orangetalents

import br.com.zup.orangetalents.commons.external.SistemaItau
import br.com.zup.orangetalents.commons.external.SistemaItauInstituicao
import br.com.zup.orangetalents.model.ChavePix
import br.com.zup.orangetalents.model.TipoChave
import br.com.zup.orangetalents.model.TipoConta
import br.com.zup.orangetalents.repositories.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class CriaChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: InsereChavePixServiceGrpc.InsereChavePixServiceBlockingStub
) {

    @Inject
    lateinit var itauCliente: SistemaItau

    var chaveDefault: ChavePix = ChavePix(
        idCliente = "5260263c-a3c1-4727-ae32-3bdb2538841b",
        tipoChave = TipoChave.EMAIL,
        tipoConta = TipoConta.POUPANCA,
        chave = "teste@teste.com"
    )

    var requestDefault: ChavePixRequest = ChavePixRequest.newBuilder()
        .setCodigo("5260263c-a3c1-4727-ae32-3bdb2538841b")
        .setChave("teste@teste.com")
        .setTipoChave(TipoChaveGrpc.EMAIL)
        .setTipoConta(TipoContaGrpc.POUPANCA)
        .build()

    @BeforeEach
    fun setUp() {
        repository.save(chaveDefault)
    }

    @AfterEach
    fun clean() {
        repository.deleteAll()
    }

    @Test
    fun `deve registrar a chave`() {
        Mockito.`when`(itauCliente.buscaPorClienteETipoConta(DadosDaContaRequest.clienteId, DadosDaContaRequest.tipo))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))
        val response = grpcClient.insere(
            ChavePixRequest.newBuilder()
                .setCodigo("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setChave("")
                .setTipoChave(TipoChaveGrpc.ALEATORIA)
                .setTipoConta(TipoContaGrpc.CORRENTE)
                .build()
        )
        with(response) {
            assertNotNull(pixId)
        }
    }

    @Test
    fun `deve verificar os campos obrigatorios`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.insere(
                ChavePixRequest.newBuilder()
                    .setCodigo("")
                    .setChave("")
                    .build()
            )
        }
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `deve verificar formatacao dos campos`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.insere(
                ChavePixRequest.newBuilder()
                    .setCodigo("")
                    .setChave("")
                    .build()
            )
        }
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `nao deve permitir conta inexistente`() {
        Mockito.`when`(itauCliente.buscaPorClienteETipoConta("asdfasdafsdaas", "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.serverError())
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.insere(
                ChavePixRequest.newBuilder()
                    .setCodigo("asdfasdafsdaas")
                    .setChave("teste@teste.com")
                    .setTipoChave(TipoChaveGrpc.EMAIL)
                    .setTipoConta(TipoContaGrpc.CORRENTE)
                    .build()
            )
        }
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
        }
    }

    @Test
    fun `nao deve permitir chave duplicada`() {
        val request = ChavePixRequest.newBuilder()
            .setCodigo(chaveDefault.idCliente)
            .setChave(chaveDefault.chave)
            .setTipoChave(TipoChaveGrpc.EMAIL)
            .setTipoConta(TipoContaGrpc.POUPANCA)
            .build()
        Mockito.`when`(itauCliente.buscaPorClienteETipoConta(request.codigo, "CONTA_POUPANCA"))
            .thenReturn(HttpResponse.ok())
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.insere(request)
        }
        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
        }
    }

    @Factory
    class ItauClient {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                InsereChavePixServiceGrpc.InsereChavePixServiceBlockingStub {
            return InsereChavePixServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(SistemaItau::class)
    fun sistemaItau(): SistemaItau? {
        return Mockito.mock(SistemaItau::class.java)
    }

    private class DadosDaContaRequest() {
        companion object {
            val clienteId: String = "c56dfef4-7901-44fb-84e2-a2cefb157890"
            val tipo: String = "CONTA_CORRENTE"
        }
    }

    private fun dadosDaContaResponse(): ClienteETipoContaResponse {
        return ClienteETipoContaResponse(
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
}