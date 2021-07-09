package br.com.zup.orangetalents

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zup.orangetalents")
		.start()
}

