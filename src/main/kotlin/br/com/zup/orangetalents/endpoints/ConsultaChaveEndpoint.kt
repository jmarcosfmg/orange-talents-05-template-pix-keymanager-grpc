package br.com.zup.orangetalents.endpoints

import br.com.zup.orangetalents.ConsultaChavePixRequest
import br.com.zup.orangetalents.ConsultaChavePixResponse
import br.com.zup.orangetalents.ConsultaChavePixServiceGrpc
import br.com.zup.orangetalents.ErrorAroundHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotNull

@ErrorAroundHandler
@Singleton
class ConsultaChaveEndpoint(
    @Inject @NotNull val chaveEndpointService: ChaveEndpointService
) : ConsultaChavePixServiceGrpc.ConsultaChavePixServiceImplBase() {

    override fun consulta(
        request: ConsultaChavePixRequest,
        responseObserver: StreamObserver<ConsultaChavePixResponse>
    ) {
        val response = when (request.filtroCase.name) {
            "CHAVE" -> chaveEndpointService.buscaChaveResponsePixPorChave(request.chave)
            else ->
                chaveEndpointService.buscaChavePixResponsePorPixIdEClienteId(request.pixId.pixId, request.pixId.clienteId)
        }
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}