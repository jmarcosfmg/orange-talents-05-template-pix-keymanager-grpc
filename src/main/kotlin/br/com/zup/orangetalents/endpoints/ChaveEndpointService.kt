package br.com.zup.orangetalents.endpoints

import br.com.zup.orangetalents.commons.exceptions.ChaveExistsViolationException
import br.com.zup.orangetalents.commons.external.bcb.RemoveChaveBCBRequest
import br.com.zup.orangetalents.commons.external.bcb.SistemaBCB
import br.com.zup.orangetalents.commons.external.bcb.criaWithExceptions
import br.com.zup.orangetalents.commons.external.bcb.removeWithExceptions
import br.com.zup.orangetalents.commons.external.itau.SistemaItau
import br.com.zup.orangetalents.commons.external.itau.buscaPorClienteETipoConta
import br.com.zup.orangetalents.model.ChavePix
import br.com.zup.orangetalents.repositories.ChavePixRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

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
}
