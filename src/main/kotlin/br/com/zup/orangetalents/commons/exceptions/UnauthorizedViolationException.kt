package br.com.zup.orangetalents.commons.exceptions

import java.lang.RuntimeException

class UnauthorizedViolationException(override val message: String = "Operação não autorizada") : RuntimeException() {

}