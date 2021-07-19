package br.com.zup.orangetalents.commons.exceptions

import java.lang.RuntimeException

class ChaveNotFoundViolationException(
    override val message: String = "Chave não encontrada") : RuntimeException() {

}
