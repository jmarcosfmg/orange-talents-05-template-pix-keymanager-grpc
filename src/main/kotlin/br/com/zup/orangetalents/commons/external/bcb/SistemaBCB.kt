package br.com.zup.orangetalents.commons.external.bcb

import br.com.zup.orangetalents.ClienteETipoContaResponse
import br.com.zup.orangetalents.commons.exceptions.ChaveExistsViolationException
import br.com.zup.orangetalents.commons.exceptions.ChaveNotFoundViolationException
import br.com.zup.orangetalents.commons.exceptions.ServerCommunicationException
import br.com.zup.orangetalents.commons.exceptions.UnauthorizedViolationException
import br.com.zup.orangetalents.commons.handlers.ValidUUID
import br.com.zup.orangetalents.model.ChavePix
import br.com.zup.orangetalents.model.TipoChave
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import javax.validation.constraints.NotBlank

@Client(value = "\${sistemaExternos.bcb}")
interface SistemaBCB {

    @Post(processes = [MediaType.APPLICATION_XML])
    fun criaChave(@Body criaChaveBCBRequest: CriaChaveBCBRequest): HttpResponse<DetalhesChaveBCBResponse>

    @Delete(processes = [MediaType.APPLICATION_XML], value = "/{key}")
    fun removeChave(
        @PathVariable key: String,
        @Body removeChaveBCBRequest: RemoveChaveBCBRequest
    ): HttpResponse<RemoveChaveBCBResponse>

    @Get(processes = [MediaType.APPLICATION_XML], value = "/{key}")
    fun buscaChave(@PathVariable key : String) : HttpResponse<DetalhesChaveBCBResponse>
}

fun SistemaBCB.criaWithExceptions(chavePix: ChavePix, cliente: ClienteETipoContaResponse) : DetalhesChaveBCBResponse{
    val chaveBCBRequest = CriaChaveBCBRequest(
        keyType = BCBKeyType.fromTipoChaveGrpc(chavePix.tipoChave.tipoChaveGrpc),
        key = chavePix.chave,
        bankAccount = BCBBankAccountRequest(
            branch = cliente.agencia,
            accountNumber = cliente.numero,
            accountType = BCBAccountType.fromTipoConta(chavePix.tipoConta).name
        ),
        ownerRequest = BCBOwnerRequest(cliente)
    )
    return (this.criaChave(chaveBCBRequest)).apply {
        if (this.status.code == 200){
            if(chavePix.tipoChave == TipoChave.ALEATORIA)
                chavePix.chave = this.body()!!.key
        }
        else if(this.status.code == 402)
            throw ChaveExistsViolationException()
        else
            throw ServerCommunicationException(RuntimeException("Não foi possível se comunicar com o servidor BCB"))
    }.body()!!
}

fun SistemaBCB.removeWithExceptions(@NotBlank @ValidUUID key: String, removeChaveBCBRequest: RemoveChaveBCBRequest) : RemoveChaveBCBOkResponse{
    return (this.removeChave(key, removeChaveBCBRequest)).let{
        it.status.run {
            if(this == HttpStatus.FORBIDDEN)
                throw UnauthorizedViolationException()
            if(this == HttpStatus.NOT_FOUND)
                throw ChaveNotFoundViolationException()
        }
        it.body() as RemoveChaveBCBOkResponse
    }
}

fun SistemaBCB.buscaChaveWithExceptions(@NotBlank @ValidUUID key: String) : DetalhesChaveBCBResponse{
    return (this.buscaChave(key)).apply {
        if(this.status.code == 404)
            throw ChaveNotFoundViolationException()
        else if (this.status.code != 200)
            throw ServerCommunicationException(RuntimeException("Não foi possível se comunicar com o servidor BCB"))
    }.body()!!
}