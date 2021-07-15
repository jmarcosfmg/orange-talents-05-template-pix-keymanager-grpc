package br.com.zup.orangetalents.model

import br.com.zup.orangetalents.TipoContaGrpc
import io.micronaut.core.annotation.Introspected

@Introspected
enum class TipoConta(value: String) {
    CORRENTE(TipoContaGrpc.CORRENTE.name),
    POUPANCA(TipoContaGrpc.POUPANCA.name)
}
