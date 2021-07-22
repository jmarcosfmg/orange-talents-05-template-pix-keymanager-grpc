package br.com.zup.orangetalents

import br.com.zup.orangetalents.commons.external.bcb.*
import br.com.zup.orangetalents.commons.external.itau.SistemaItau
import br.com.zup.orangetalents.commons.external.itau.SistemaItauInstituicao
import br.com.zup.orangetalents.model.ChavePix
import br.com.zup.orangetalents.model.TipoChave
import br.com.zup.orangetalents.model.TipoConta
import br.com.zup.orangetalents.repositories.ChavePixRepository
import com.google.type.DateTime
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.mockito.Mockito
import javax.inject.Inject

@Nested
@MicronautTest(transactional = false)
internal class CriaChaveEndpointTest(
    @Inject val repository: ChavePixRepository,
    @Inject val InsereChaveClient: InsereChavePixServiceGrpc.InsereChavePixServiceBlockingStub
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

    var bcbRequestDefault = CriaChaveBCBRequest(
        keyType = BCBKeyType.EMAIL,
        key = "teste@teste.com",
        bankAccount = BCBBankAccountRequest(
            branch = "0001",
            accountNumber = "291900",
            accountType = BCBAccountType.SVGS.name
        ),
        ownerRequest = BCBOwnerRequest(
            name = "Yuri Matheus",
            taxIdNumber = "86135457004"
        )
    )

    var bcbResponseDefault = CriaChaveBCBResponse(
        keyType = BCBKeyType.EMAIL.name,
        key = "teste@teste.com",
        bankAccount = BCBBankAccountResponse(
            branch = "0001",
            accountNumber = "291900",
            participant = "60701190",
            accountType = BCBAccountType.SVGS.name
        ),
        owner = BCBOwnerResponse(
            type = "NATURAL_PERSON",
            name = "Yuri Matheus",
            taxIdNumber = "86135457004"
        ),
        createdAt = DateTime.getDefaultInstance()
    )

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

        val novaChaveItauResponse = ClienteETipoContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = SistemaItauInstituicao(
                nome = "ITAÚ UNIBANCO S.A.",
                ispb = "60701190"),
            agencia = "0001",
            numero = "291900",
            titular = SistemaItauTitular(
                id = "c56dfef4-7901-44fb-84e2-a2cefb157890",
                nome = "Rafael M C Ponte",
                cpf = "02467781054"
            )
        )

        val novaChaveBCBResponse = CriaChaveBCBResponse(
            keyType = BCBKeyType.EMAIL.name,
            key = "teste2@teste.com",
            bankAccount = BCBBankAccountResponse(
                branch = "0001",
                accountNumber = "291900",
                participant = "60701190",
                accountType = BCBAccountType.SVGS.name
            ),
            owner = BCBOwnerResponse(
                type = "NATURAL_PERSON",
                name = "Rafael M C Ponte",
                taxIdNumber = "86135457004"
            ),
            createdAt = DateTime.getDefaultInstance()
        )

        val novaChaveBCBRequest = CriaChaveBCBRequest(
            keyType = BCBKeyType.EMAIL,
            key = "teste2@teste.com",
            bankAccount = BCBBankAccountRequest(
                branch = "0001",
                accountNumber = "291900",
                accountType = BCBAccountType.CACC.name
            ),
            ownerRequest = BCBOwnerRequest(
                name = "Rafael M C Ponte",
                taxIdNumber = "02467781054"
            )
        )

        Mockito.`when`(itauCliente.buscaContaCliente(novaChaveItauResponse.titular.id, novaChaveItauResponse.tipo))
            .thenReturn(HttpResponse.ok(novaChaveItauResponse))
        Mockito.`when`(bcbCliente.criaChave(novaChaveBCBRequest))
            .thenReturn(HttpResponse.ok(novaChaveBCBResponse))
        val response = InsereChaveClient.insere(
            ChavePixRequest.newBuilder()
                .setCodigo("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setChave("teste2@teste.com")
                .setTipoChave(TipoChaveGrpc.EMAIL)
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
            InsereChaveClient.insere(
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
            InsereChaveClient.insere(
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
        repository.deleteAll()
        Mockito.`when`(itauCliente.buscaContaCliente("asdfasdafsdaas", "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.serverError())
        val thrown = assertThrows<StatusRuntimeException> {
            InsereChaveClient.insere(
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
        Mockito.`when`(itauCliente.buscaContaCliente(request.codigo, "CONTA_POUPANCA"))
            .thenReturn(HttpResponse.ok())
        val thrown = assertThrows<StatusRuntimeException> {
            InsereChaveClient.insere(request)
        }
        with(thrown) {
            println(status.description)
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

}