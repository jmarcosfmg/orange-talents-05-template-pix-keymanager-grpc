package br.com.zup.orangetalents.endpoints

import br.com.zup.orangetalents.ErrorAroundHandler
import br.com.zup.orangetalents.ListaChavePixRequest
import br.com.zup.orangetalents.ListaChavePixResponse
import br.com.zup.orangetalents.ListaChavePixServiceGrpc
import br.com.zup.orangetalents.model.ChavePix
import br.com.zup.orangetalents.repositories.ChavePixRepository
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorAroundHandler
@Singleton
class ListaChaveEndpoint(
                         @Inject val chavePixRepository: ChavePixRepository
) :ListaChavePixServiceGrpc.ListaChavePixServiceImplBase(){
    override fun lista(request: ListaChavePixRequest, responseObserver: StreamObserver<ListaChavePixResponse>) {
        val resultList = chavePixRepository.findAllByIdCliente(request.clienteId).map {it.toChavePixResponse()}.run {
            ListaChavePixResponse.newBuilder().addAllChaves(this).build()
        }
        responseObserver.onNext(resultList)
        responseObserver.onCompleted()
    }

    fun ChavePix.toChavePixResponse(): ListaChavePixResponse.Chave{
        return ListaChavePixResponse.Chave.newBuilder()
            .setChave(this.chave)
            .setClienteId(this.idCliente)
            .setId(this.id.toString())
            .setTipoChave(this.tipoChave.toTipoGrpc())
            .setTipoConta(this.tipoConta.toTipoGrpc())
            .setCriadaEm(Timestamp.newBuilder().setNanos(this.criadaEm?.nano?: 0).setSeconds((this.criadaEm?.second?:0).toLong()).build())
            .build()
    }
}