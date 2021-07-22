package br.com.zup.orangetalents.commons.exceptions

class ChaveExistsViolationException(override val message : String = "Chave já existe") : RuntimeException() {

}