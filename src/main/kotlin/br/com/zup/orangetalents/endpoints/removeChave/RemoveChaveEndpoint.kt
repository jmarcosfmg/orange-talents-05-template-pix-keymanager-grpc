package br.com.zup.orangetalents.endpoints.removeChave

import br.com.zup.orangetalents.ErrorAroundHandler
import br.com.zup.orangetalents.RemoveChavePixRequest
import br.com.zup.orangetalents.RemoveChavePixResponse
import br.com.zup.orangetalents.RemoveChavePixServiceGrpc
import br.com.zup.orangetalents.RemoveChavePixServiceGrpc.*
import br.com.zup.orangetalents.commons.exceptions.ChaveNotFoundViolationException
import br.com.zup.orangetalents.commons.exceptions.ClienteNotFoundViolationException
import br.com.zup.orangetalents.commons.exceptions.UnauthorizedViolationException
import br.com.zup.orangetalents.commons.external.SistemaItau
import br.com.zup.orangetalents.model.ChavePix
import br.com.zup.orangetalents.repositories.ChavePixRepository
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
@ErrorAroundHandler
class RemoveChaveEndpoint(
    @Inject val sistemaItau: SistemaItau,
    @Inject val chavePixRepository: ChavePixRepository
) :
    RemoveChavePixServiceImplBase() {

    override fun remove(request: RemoveChavePixRequest, responseObserver: StreamObserver<RemoveChavePixResponse>) {
        sistemaItau.buscaPorCliente(request.clientId).run {
            if (this.status.code != HttpStatus.OK.code)
                throw ClienteNotFoundViolationException()
        }
        val chave = (chavePixRepository.findById(UUID.fromString(request.pixId)).takeIf {
            it.isPresent
        } ?: throw ChaveNotFoundViolationException()).get()

        if (request.clientId != chave.idCliente)
            throw UnauthorizedViolationException("Chave não pertence ao cliente")

        transactionalRemove(chavePixRepository, chave)
        responseObserver.onNext(RemoveChavePixResponse.getDefaultInstance())
        responseObserver.onCompleted()
    }

    @Transactional
    fun transactionalRemove(chavePixRepository: ChavePixRepository, chavePix: ChavePix) {
        chavePixRepository.delete(chavePix)
    }
}
