package br.com.zup.orangetalents

import br.com.zup.orangetalents.commons.external.bcb.*
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
internal class ConsultaChaveEndpointTest(
    @Inject val repository: ChavePixRepository,
    @Inject val consultaChaveClient: ConsultaChavePixServiceGrpc.ConsultaChavePixServiceBlockingStub
) {
    @Inject
    lateinit var itauCliente: SistemaItau

    @Inject
    lateinit var bcbCliente: SistemaBCB

    lateinit var pixId: UUID

    val chaveDefault: ChavePix = ChavePix(
        idCliente = "5260263c-a3c1-4727-ae32-3bdb2538841b",
        tipoChave = TipoChave.EMAIL,
        tipoConta = TipoConta.POUPANCA,
        chave = "teste@teste.com"
    )

    val itauBuscaPorClienteResponse = ClienteResponse(
        instituicao = SistemaItauInstituicao(nome = "ITAÚ UNIBANCO S.A.", ispb = "60701190"),
        id = "5260263c-a3c1-4727-ae32-3bdb2538841b",
        nome = "Yuri Matheus",
        cpf = "86135457004"
    )

    val clienteETipoContaResponse = ClienteETipoContaResponse(
        tipo = "CONTA_POUPANCA",
        instituicao = SistemaItauInstituicao(
            nome = "ITAÚ UNIBANCO S.A.",
            ispb = "60701190"
        ),
        agencia = "0001",
        numero = "291900",
        titular = SistemaItauTitular(
            id = "5260263c-a3c1-4727-ae32-3bdb2538841b",
            nome = "Yuri Matheus",
            cpf = "86135457004"
        )
    )

    @BeforeEach
    fun setUp() {
        pixId = repository.save(chaveDefault).id
    }

    @AfterEach
    fun clean() {
        repository.deleteAll()
    }

    @Test
    fun `deve buscar pela chave Pix`() {
        Mockito.`when`(
            itauCliente.buscaContaCliente(
                chaveDefault.idCliente, "CONTA_${chaveDefault.tipoConta.name}"
            )

        ).thenReturn(
            HttpResponse.ok(clienteETipoContaResponse)
        )

        val response: ConsultaChavePixResponse =
            consultaChaveClient.consulta(ConsultaChavePixRequest.newBuilder().setChave(chaveDefault.chave).build())

        Assertions.assertEquals(response.pixId, pixId.toString())
        Assertions.assertEquals(response.clienteId, chaveDefault.idCliente)
        Assertions.assertEquals(response.chave.chave, chaveDefault.chave)
        Assertions.assertEquals(response.chave.conta.cpfDoTitular, itauBuscaPorClienteResponse.cpf)
        Assertions.assertEquals(response.chave.conta.tipo.name, chaveDefault.tipoConta.name)
    }

    @Test
    fun `deve buscar pelo keymanager`() {
        Mockito.`when`(
            itauCliente.buscaContaCliente(
                chaveDefault.idCliente, "CONTA_${chaveDefault.tipoConta.name}"
            )
        ).thenReturn(
            HttpResponse.ok(clienteETipoContaResponse)
        )

        Mockito.`when`(
            bcbCliente.buscaChave(pixId.toString())
        ).thenReturn(
            HttpResponse.ok(
                DetalhesChaveBCBResponse(
                    keyType = chaveDefault.tipoChave.name,
                    key = chaveDefault.chave,
                    bankAccount = BCBBankAccountResponse(
                        participant = clienteETipoContaResponse.instituicao.ispb,
                        branch = clienteETipoContaResponse.agencia,
                        accountNumber = clienteETipoContaResponse.numero,
                        accountType = BCBAccountType.fromTipoConta(chaveDefault.tipoConta).name
                    ),
                    owner = BCBOwnerResponse(
                        type = "NATURAL_PERSON",
                        name = clienteETipoContaResponse.titular.nome,
                        taxIdNumber = clienteETipoContaResponse.titular.cpf
                    ),
                    createdAt = LocalDateTime.now()
                )
            )
        )

        val response: ConsultaChavePixResponse =
            consultaChaveClient.consulta(
                ConsultaChavePixRequest.newBuilder()
                    .setPixId(
                        ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                            .setClienteId(chaveDefault.idCliente)
                            .setPixId(pixId.toString())
                            .build()
                    ).build()
            )

        Assertions.assertEquals("", response.pixId)
        Assertions.assertEquals("", response.clienteId)
        Assertions.assertEquals(chaveDefault.chave, response.chave.chave)
        Assertions.assertEquals(itauBuscaPorClienteResponse.cpf, response.chave.conta.cpfDoTitular)
        Assertions.assertEquals(chaveDefault.tipoConta.name, response.chave.conta.tipo.name)
    }

    @Test
    fun `deve dar erro se a chave nao existir`() {
        val newPixId = UUID.randomUUID().toString()
        Mockito.`when`(
            bcbCliente.buscaChave(newPixId)
        ).thenReturn(
            HttpResponse.notFound()
        )

        val thrownKey = assertThrows<StatusRuntimeException> {
            consultaChaveClient.consulta(
                ConsultaChavePixRequest
                    .newBuilder().setChave(newPixId).build()
            )
        }

        val thrownChavePix = assertThrows<StatusRuntimeException> {
            consultaChaveClient.consulta(
                ConsultaChavePixRequest.newBuilder()
                    .setPixId(
                        ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                            .setClienteId(chaveDefault.idCliente)
                            .setPixId(newPixId)
                            .build()
                    ).build()
            )
        }

        Assertions.assertEquals(Status.NOT_FOUND.code, thrownChavePix.status.code)
        Assertions.assertEquals(Status.NOT_FOUND.code, thrownKey.status.code)
    }

    @Test
    fun `deve dar erro se a chave nao pertencer ao cliente`() {
        val thrownChavePix = assertThrows<StatusRuntimeException> {
            consultaChaveClient.consulta(
                ConsultaChavePixRequest.newBuilder()
                    .setPixId(
                        ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                            .setPixId(pixId.toString())
                            .build()
                    ).build()
            )
        }
        Assertions.assertEquals(thrownChavePix.status.code, Status.PERMISSION_DENIED.code)
    }

    @Test
    fun `deve buscar no bcb se a chave nao for encontrada no sistema`() {
        val newPixId = UUID.randomUUID().toString()
        Mockito.`when`(
            bcbCliente.buscaChave(newPixId)
        ).thenReturn(
            HttpResponse.ok(
                DetalhesChaveBCBResponse(
                    keyType = chaveDefault.tipoChave.name,
                    key = chaveDefault.chave,
                    bankAccount = BCBBankAccountResponse(
                        participant = clienteETipoContaResponse.instituicao.ispb,
                        branch = clienteETipoContaResponse.agencia,
                        accountNumber = clienteETipoContaResponse.numero,
                        accountType = BCBAccountType.fromTipoConta(chaveDefault.tipoConta).name
                    ),
                    owner = BCBOwnerResponse(
                        type = "NATURAL_PERSON",
                        name = clienteETipoContaResponse.titular.nome,
                        taxIdNumber = clienteETipoContaResponse.titular.cpf
                    ),
                    createdAt = LocalDateTime.now()
                )
            )
        )

    }

    @Test
    fun `deve dar erro se a chave nao estiver no bcb`() {

        Mockito.`when`(
            bcbCliente.buscaChave(pixId.toString())
        ).thenReturn(HttpResponse.notFound())

        val thrown = assertThrows<StatusRuntimeException> {
            consultaChaveClient.consulta(
                ConsultaChavePixRequest.newBuilder()
                    .setPixId(
                        ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                            .setClienteId(chaveDefault.idCliente)
                            .setPixId(pixId.toString())
                            .build()
                    ).build()
            )
        }
        Assertions.assertEquals(Status.PERMISSION_DENIED.code, thrown.status.code)
        Assertions.assertEquals("PERMISSION_DENIED: Chave inválida - Aguardando aprovação!", thrown.message)
    }

    @MockBean(SistemaItau::class)
    fun sistemaItau(): SistemaItau? {
        return Mockito.mock(SistemaItau::class.java)
    }

    @MockBean(SistemaBCB::class)
    fun sistemaBCB(): SistemaBCB? {
        return Mockito.mock(SistemaBCB::class.java)
    }

    @Factory
    class GrpcConsultaClient {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                ConsultaChavePixServiceGrpc.ConsultaChavePixServiceBlockingStub {
            return ConsultaChavePixServiceGrpc.newBlockingStub(channel)
        }
    }

}