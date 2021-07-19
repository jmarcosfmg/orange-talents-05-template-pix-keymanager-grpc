package br.com.zup.orangetalents.commons.exceptions

class ClienteNotFoundViolationException(
    override val message: String = "Cliente não encontrado"): RuntimeException() {

}
