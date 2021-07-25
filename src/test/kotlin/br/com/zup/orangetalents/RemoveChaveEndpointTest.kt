package br.com.zup.orangetalents

import br.com.zup.orangetalents.commons.external.bcb.RemoveChaveBCBOkResponse
import br.com.zup.orangetalents.commons.external.bcb.RemoveChaveBCBRequest
import br.com.zup.orangetalents.commons.external.bcb.SistemaBCB
import br.com.zup.orangetalents.commons.external.itau.ClienteResponse
import br.com.zup.orangetalents.commons.external.itau.SistemaItau
import br.com.zup.orangetalents.commons.external.itau.SistemaItauInstituicao
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
import org.junit.jupiter.api.*
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@Nested
@MicronautTest(transactional = false)
internal class RemoveChaveEndpointTest(
    @Inject val repository: ChavePixRepository,
    @Inject val removeChaveClient: RemoveChavePixServiceGrpc.RemoveChavePixServiceBlockingStub
) {
    @Inject
    lateinit var itauCliente: SistemaItau

    @Inject
    lateinit var bcbCliente: SistemaBCB

    var chaveDefault: ChavePix = ChavePix(
        idCliente = "5260263c-a3c1-4727-ae32-3bdb2538841b",
        tipoChave = TipoChave.EMAIL,
        tipoConta = TipoConta.POUPANCA,
        chave = "teste@teste.com"
    )

    val clienteResponse = ClienteResponse(
        instituicao = SistemaItauInstituicao(nome = "ITAÚ UNIBANCO S.A.", ispb = "60701190"),
        id = "5260263c-a3c1-4727-ae32-3bdb2538841b",
        nome = "Yuri Matheus",
        cpf = "86135457004"
    )

    lateinit var pixId: UUID

    @BeforeEach
    fun setUp() {
        pixId = repository.save(chaveDefault).id
    }

    @AfterEach
    fun clean() {
        repository.deleteAll()
    }

    @Test
    fun `deve remover chave`() {
        Mockito.`when`(
            itauCliente.buscaCliente(
                chaveDefault.idCliente
            )
        ).thenReturn(
            HttpResponse.ok(clienteResponse)
        )

        Mockito.`when`(
            bcbCliente.removeChave(
                key = chaveDefault.chave,
                removeChaveBCBRequest = RemoveChaveBCBRequest(key = chaveDefault.chave)
            )
        ).thenReturn(
            HttpResponse.ok(
                RemoveChaveBCBOkResponse(
                    key = chaveDefault.chave,
                    participant = "60701190",
                    deletedAt = LocalDateTime.now()
                )
            )
        )

        val response = removeChaveClient.remove(
            RemoveChavePixRequest.newBuilder()
                .setClientId(chaveDefault.idCliente)
                .setPixId(pixId.toString()).build()
        )

        Assertions.assertFalse(repository.existsById(pixId))
    }

    @Test
    fun `nao deve remover chave inexistente`() {
        Mockito.`when`(
            itauCliente.buscaCliente(DadosDaContaRequest.clienteId)
        ).thenReturn(HttpResponse.ok(dadosDaContaResponse()))
        val thrown = assertThrows<StatusRuntimeException> {
            removeChaveClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClientId(DadosDaContaRequest.clienteId)
                    .setPixId(UUID.randomUUID().toString()).build()
            )
        }
        with(thrown) {
            Assertions.assertEquals(Status.NOT_FOUND.code, thrown.status.code)
        }
    }

    @Test
    fun `nao deve remover chave de outra pessoa`() {
        Mockito.`when`(itauCliente.buscaCliente(DadosDaContaRequest.clienteId))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))
        val thrown = assertThrows<StatusRuntimeException> {
            removeChaveClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClientId(dadosDaContaResponse().id)
                    .setPixId(pixId.toString()).build()
            )
        }
        with(thrown) {
            Assertions.assertEquals(Status.PERMISSION_DENIED.code, thrown.status.code)
            Assertions.assertTrue(repository.existsById(pixId))
        }
    }

    @Test
    fun `nao deve remover se chave nao existir no bcb`() {

        val removeChaveBCBRequest = RemoveChaveBCBRequest(chaveDefault.chave)

        Mockito.`when`(itauCliente.buscaCliente(chaveDefault.idCliente))
            .thenReturn(HttpResponse.ok(clienteResponse))
        Mockito.`when`(bcbCliente.removeChave(chaveDefault.chave, removeChaveBCBRequest))
            .thenReturn(HttpResponse.notFound())
        val thrown = assertThrows<StatusRuntimeException> {
            removeChaveClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClientId(chaveDefault.idCliente)
                    .setPixId(pixId.toString()).build()
            )
        }
        with(thrown) {
            Assertions.assertEquals(Status.NOT_FOUND.code, thrown.status.code)
        }
    }

    @Factory
    class GrpcClient {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                RemoveChavePixServiceGrpc.RemoveChavePixServiceBlockingStub {
            return RemoveChavePixServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(SistemaItau::class)
    fun sistemaItau(): SistemaItau? {
        return Mockito.mock(SistemaItau::class.java)
    }

    @MockBean(SistemaBCB::class)
    fun sistemaBCB(): SistemaBCB? {
        return Mockito.mock(SistemaBCB::class.java)
    }

    private class DadosDaContaRequest() {
        companion object {
            val clienteId: String = "c56dfef4-7901-44fb-84e2-a2cefb157890"
            val tipo: String = "CONTA_CORRENTE"
        }
    }

    private fun dadosDaContaResponse(): ClienteResponse {
        return ClienteResponse(
            instituicao = SistemaItauInstituicao(nome = "ITAÚ UNIBANCO S.A.", ispb = "60701190"),
            id = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            nome = "Rafael M C Ponte",
            cpf = "02467781054"
        )
    }
}