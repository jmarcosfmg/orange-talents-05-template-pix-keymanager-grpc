package br.com.zup.orangetalents.endpoints.criaChave

import br.com.zup.orangetalents.*
import br.com.zup.orangetalents.commons.handlers.GenericValidator
import br.com.zup.orangetalents.model.ChavePix
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
@ErrorAroundHandler
@Transactional
open class CriaChaveEndpoint(
    @Inject val validator: GenericValidator,
    @Inject val sistemaItau: SistemaItau,
    @Inject val chavePixRepository: ChavePixRepository
) :
    ChavePixServiceGrpc.ChavePixServiceImplBase() {

    override fun insere(request: ChavePixRequest, responseObserver: StreamObserver<ChavePixResponse>) {
        val novaChave: ChavePix = ChavePix.fromRequest(request)
        validator.validate(novaChave)
        novaChave.tipoChave.validate(novaChave.chave)
        sistemaItau.contaExists(request)
        val chaveSalva = chavePixRepository.save(novaChave)
        responseObserver.onNext(
            ChavePixResponse.newBuilder()
                .setPixId(chaveSalva.id.toString())
                .build()
        )
        responseObserver.onCompleted()
    }
}