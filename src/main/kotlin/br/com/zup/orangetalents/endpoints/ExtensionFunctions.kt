package br.com.zup.orangetalents.endpoints

import br.com.zup.orangetalents.ClienteETipoContaResponse
import br.com.zup.orangetalents.ConsultaChavePixResponse
import br.com.zup.orangetalents.commons.external.bcb.BCBAccountType
import br.com.zup.orangetalents.commons.external.bcb.BCBKeyType
import br.com.zup.orangetalents.commons.external.bcb.DetalhesChaveBCBResponse
import br.com.zup.orangetalents.model.ChavePix

class ExtensionFunctions {


}

fun ConsultaChavePixResponse.Builder.buildFromChaveAndCliente(
    chave: ChavePix,
    clienteETipoConta: ClienteETipoContaResponse
): ConsultaChavePixResponse {
    return this.setClienteId(clienteETipoConta.titular.id)
        .setPixId(chave.id.toString())
        .setChave(
            ConsultaChavePixResponse.ChavePix.newBuilder()
                .setTipo(chave.tipoChave.toTipoGrpc())
                .setChave(chave.chave)
                .setConta(
                    ConsultaChavePixResponse.ChavePix.ContaInfo.newBuilder()
                        .setTipo(chave.tipoConta.toTipoGrpc())
                        .setInstituicao(clienteETipoConta.instituicao.nome)
                        .setNomeDoTitular(clienteETipoConta.titular.nome)
                        .setCpfDoTitular(clienteETipoConta.titular.cpf)
                        .setAgencia(clienteETipoConta.agencia)
                        .setNumeroDaConta(clienteETipoConta.numero).build()
                ).build()
        )
        .build()
}

fun ConsultaChavePixResponse.Builder.buildFromDetalhesChaveBCBResponse(
    detalhesChaveBCBResponse : DetalhesChaveBCBResponse
): ConsultaChavePixResponse {
    return this.setChave(
            ConsultaChavePixResponse.ChavePix.newBuilder()
                .setTipo(
                    BCBKeyType.valueOf(detalhesChaveBCBResponse.keyType)
                        .tipoChave.toTipoGrpc()
                )
                .setChave(detalhesChaveBCBResponse.key)
                .setConta(
                    ConsultaChavePixResponse.ChavePix.ContaInfo.newBuilder()
                        .setTipo(BCBAccountType.valueOf(detalhesChaveBCBResponse
                            .bankAccount.accountType).tipoConta.toTipoGrpc())
                        .setInstituicao(detalhesChaveBCBResponse.bankAccount.participant)
                        .setNomeDoTitular(detalhesChaveBCBResponse.owner.name)
                        .setCpfDoTitular(detalhesChaveBCBResponse.owner.taxIdNumber)
                        .setAgencia(detalhesChaveBCBResponse.bankAccount.branch)
                        .setNumeroDaConta(detalhesChaveBCBResponse.bankAccount.accountNumber)
                        .build()
                ).build()
        )
        .build()
}