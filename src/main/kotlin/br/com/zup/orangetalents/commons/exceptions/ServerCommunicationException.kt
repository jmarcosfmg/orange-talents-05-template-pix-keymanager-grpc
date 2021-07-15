package br.com.zup.orangetalents.commons.exceptions

class ServerCommunicationException(exception: Exception) : RuntimeException(exception.localizedMessage) {
}