package br.com.zup.orangetalents.endpoints

import br.com.zup.orangetalents.ConsultaChavePixResponse
import br.com.zup.orangetalents.commons.exceptions.ChaveExistsViolationException
import br.com.zup.orangetalents.commons.exceptions.ChaveNotFoundViolationException
import br.com.zup.orangetalents.commons.exceptions.UnauthorizedViolationException
import br.com.zup.orangetalents.commons.external.bcb.*
import br.com.zup.orangetalents.commons.external.itau.SistemaItau
import br.com.zup.orangetalents.commons.external.itau.buscaPorClienteETipoConta
import br.com.zup.orangetalents.commons.handlers.ValidUUID
import br.com.zup.orangetalents.model.ChavePix
import br.com.zup.orangetalents.repositories.ChavePixRepository
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Validated
@Singleton
open class ChaveEndpointService(
    @Inject
    val sistemaItau: SistemaItau,
    @Inject
    val chavePixRepository: ChavePixRepository,
    @Inject
    val sistemaBCB: SistemaBCB
) {
    @Transactional
    fun transactionalInsere(
        @Valid chavePix: ChavePix
    ): ChavePix {
        if (chavePixRepository.buscaPorChaveETipo(chavePix.chave, chavePix.tipoChave).isNotEmpty())
            throw ChaveExistsViolationException("Chave já registrada")
        val chave = chavePixRepository.save(chavePix)
        val cliente = sistemaItau.buscaPorClienteETipoConta(chave.idCliente, chave.tipoConta.name)
        sistemaBCB.criaWithExceptions(chave, cliente)
        return chave
    }

    @Transactional
    fun transactionalRemove(chavePix: ChavePix) {
        chavePixRepository.delete(chavePix)
        sistemaBCB.removeWithExceptions(
            key = chavePix.chave,
            removeChaveBCBRequest = RemoveChaveBCBRequest(
                key = chavePix.chave
            )
        )
    }

    fun buscaChaveResponsePixPorChave(@NotBlank @ValidUUID key: String): ConsultaChavePixResponse {
        val chave = chavePixRepository.findByChave(key).apply {
            if (this.isEmpty) {
                return@buscaChaveResponsePixPorChave sistemaBCB.buscaChaveWithExceptions(key).let {
                    ConsultaChavePixResponse.newBuilder()
                        .buildFromDetalhesChaveBCBResponse(it)
                }
            }
        }.get()
        val cliente = sistemaItau.buscaPorClienteETipoConta(chave.idCliente, chave.tipoConta.name)
        return ConsultaChavePixResponse.newBuilder()
            .buildFromChaveAndCliente(chave, cliente)
    }

    fun buscaChavePixResponsePorPixIdEClienteId(@NotBlank @ValidUUID key: String, @NotBlank @ValidUUID clientId: String): ConsultaChavePixResponse {
        chavePixRepository.findById(UUID.fromString(key)).orElseThrow {
            throw ChaveNotFoundViolationException()
        }.run {
            if (this.idCliente != clientId)
                throw UnauthorizedViolationException("Chave não pertence ao cliente")
        }

        return try {
            sistemaBCB.buscaChaveWithExceptions(key)
        } catch (e: RuntimeException) {
            if (e is ChaveNotFoundViolationException)
                throw UnauthorizedViolationException("Chave inválida - Aguardando aprovação!")
            throw e
        }.run {
            ConsultaChavePixResponse.newBuilder()
                .buildFromDetalhesChaveBCBResponse(this)
        }
    }
}
