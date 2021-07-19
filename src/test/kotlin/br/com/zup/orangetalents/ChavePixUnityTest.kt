package br.com.zup.orangetalents

import br.com.zup.orangetalents.repositories.ChavePixRepository
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@Nested
@MicronautTest(transactional = false)
internal class ChavePixUnityTest(
    val chavePixRepository: ChavePixRepository,
    val grpcClient: InsereChavePixServiceGrpc.InsereChavePixServiceBlockingStub
) {

    @AfterEach
    fun clean() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve verificar formatacao CPF`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.insere(
                ChavePixRequest.newBuilder()
                    .setTipoConta(TipoContaGrpc.POUPANCA)
                    .setTipoChave(TipoChaveGrpc.CPF).setChave("1597538524o")
                    .setCodigo("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .build()
            )
        }
        with(thrown){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `deve verificar formatacao email`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.insere(
                ChavePixRequest.newBuilder()
                    .setTipoConta(TipoContaGrpc.POUPANCA)
                    .setTipoChave(TipoChaveGrpc.EMAIL).setChave("testeteste.com")
                    .setCodigo("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .build()
            )
        }
        with(thrown){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `deve verificar formatacao telefone`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.insere(
                ChavePixRequest.newBuilder()
                    .setTipoConta(TipoContaGrpc.POUPANCA)
                    .setTipoChave(TipoChaveGrpc.CELULAR).setChave("5587455786")
                    .setCodigo("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .build()
            )
        }
        with(thrown){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }
}