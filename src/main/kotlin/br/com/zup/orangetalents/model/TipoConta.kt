package br.com.zup.orangetalents.model

import br.com.zup.orangetalents.TipoContaGrpc
import io.micronaut.core.annotation.Introspected

@Introspected
enum class TipoConta(val tipoContaGrpc: String) {
    CORRENTE(TipoContaGrpc.CORRENTE.name),
    POUPANCA(TipoContaGrpc.POUPANCA.name);


    fun toTipoGrpc(): TipoContaGrpc {
        return (TipoContaGrpc.values().firstOrNull {
            it.name == this.tipoContaGrpc
        }) ?:
        throw RuntimeException("Tipo de chave não existe no sistema")
    }
}