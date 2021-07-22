package br.com.zup.orangetalents.endpoints

import br.com.zup.orangetalents.ChavePixRequest
import br.com.zup.orangetalents.ChavePixResponse
import br.com.zup.orangetalents.ErrorAroundHandler
import br.com.zup.orangetalents.InsereChavePixServiceGrpc
import br.com.zup.orangetalents.commons.handlers.GenericValidator
import br.com.zup.orangetalents.model.ChavePix
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorAroundHandler
open class CriaChaveEndpoint(
    @Inject val chaveService : ChaveEndpointService
) : InsereChavePixServiceGrpc.InsereChavePixServiceImplBase() {

    override fun insere(request: ChavePixRequest, responseObserver: StreamObserver<ChavePixResponse>) {
        val chaveSalva: ChavePix = ChavePix.fromRequest(request).takeIf {
            it.tipoChave.validate(it.chave)
        }.let {
            chaveService.transactionalInsere(it!!)
        }
        responseObserver.onNext(
            ChavePixResponse.newBuilder()
                .setPixId(chaveSalva.id.toString())
                .build()
        )
        responseObserver.onCompleted()
    }
}