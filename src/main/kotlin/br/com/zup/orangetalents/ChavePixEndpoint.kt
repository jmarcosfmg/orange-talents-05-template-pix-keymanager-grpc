package br.com.zup.orangetalents

import br.com.zup.orangetalents.handlers.ChaveExistsViolationException
import br.com.zup.orangetalents.handlers.ChaveFormatViolationException
import br.com.zup.orangetalents.handlers.GenericValidator
import br.com.zup.orangetalents.model.ChavePix
import br.com.zup.orangetalents.model.TipoChave
import br.com.zup.orangetalents.model.TipoConta
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.*
import javax.transaction.Transactional

@Singleton
@ErrorAroundHandler
@Transactional
open class ChavePixEndpoint(@Inject val validator: GenericValidator, @Inject val chavePixRepository: ChavePixRepository) :
    ChavePixServiceGrpc.ChavePixServiceImplBase() {

    override fun insere(request: ChavePixRequest, responseObserver: StreamObserver<ChavePixResponse>) {
        val novaChave: ChavePix = request.let {
            val nova = ChavePix(
                "asdfasdafsd",
                TipoChave.toModel(it.tipoChave),
                TipoConta.valueOf(it.tipoConta.name),
                if(it.tipoChave == TipoChaveGrpc.ALEATORIA) UUID.randomUUID().toString() else it.chave
            )
            if (!validator.validate(nova) || !nova.tipoChave.isValid(nova.chave)) {
                throw ChaveFormatViolationException("${nova.tipoChave.name} inválido!")
            }
            return@let nova
        }
        if (chavePixRepository.buscaPorChaveETipo(novaChave.chave, novaChave.tipoChave).isNotEmpty())
            throw ChaveExistsViolationException("${novaChave.tipoChave.name} inválido!")

        chavePixRepository.save(novaChave)

        responseObserver.onNext(ChavePixResponse.newBuilder().setPixId(novaChave.id.toString()).build())
        responseObserver.onCompleted()
    }
}