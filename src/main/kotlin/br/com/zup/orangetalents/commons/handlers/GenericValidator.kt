package br.com.zup.orangetalents.commons.handlers

import io.micronaut.core.annotation.Introspected
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.validation.ConstraintViolationException
import javax.validation.Validator

@Singleton
@Introspected
class GenericValidator(@Inject val validator: Validator) {
    fun validate(element: Any): Boolean {
        val errors = validator.validate(element)
        if (errors.isEmpty()) return true;
        throw ConstraintViolationException(errors)
    }
}