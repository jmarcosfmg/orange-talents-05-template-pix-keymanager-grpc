package br.com.zup.orangetalents.endpoints.criaChave

import br.com.zup.orangetalents.*
import br.com.zup.orangetalents.commons.external.SistemaItau
import br.com.zup.orangetalents.commons.handlers.GenericValidator
import br.com.zup.orangetalents.model.ChavePix
import br.com.zup.orangetalents.repositories.ChavePixRepository
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
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
        val chaveSalva: ChavePix = ChavePix.fromRequest(request).takeIf {
                validator.validate(it) && it.tipoChave.validate(it.chave) && sistemaItau.contaExists(request)
        }.let { chavePixRepository.save(it!!) }
        responseObserver.onNext(
            ChavePixResponse.newBuilder()
                .setPixId(chaveSalva.id.toString())
                .build()
        )
        responseObserver.onCompleted()
    }
}