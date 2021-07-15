package br.com.zup.orangetalents.model

import br.com.zup.orangetalents.TipoChaveGrpc
import br.com.zup.orangetalents.handlers.ChaveFormatViolationException
import io.micronaut.core.annotation.Introspected
import javax.validation.ConstraintViolationException

@Introspected
enum class TipoChave(private val regex: Regex, val tipoChaveGrpc: TipoChaveGrpc) {

    CPF(Regex("^[0-9]{11}\$"), TipoChaveGrpc.CPF),
    CELULAR(Regex("^\\+[1-9][0-9]\\d{1,14}\$"), TipoChaveGrpc.CELULAR),
    EMAIL(
        Regex("^[a-zA-Z0-9.!#\$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*\$"),
        TipoChaveGrpc.EMAIL
    ),
    ALEATORIA(Regex(".*"), TipoChaveGrpc.ALEATORIA);

    companion object {
        @Throws(ConstraintViolationException::class)
        fun toModel(chaveGrpc: TipoChaveGrpc): TipoChave {
            for (tipoChave in values()) {
                if (chaveGrpc.name.equals(tipoChave.tipoChaveGrpc.name, true))
                    return tipoChave
            }
            throw ChaveFormatViolationException("Tipo de chave inválido")
        }
    }

    open fun isValid(str: String): Boolean {
        return this.regex.matches(str)
    }

}
