package br.com.zup.orangetalents.commons.handlers

import java.util.*
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext


@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
@Constraint(validatedBy = [ValidUUIDValidator::class])
annotation class ValidUUID(
    val message: String = "Id inválido"
)

@Singleton
class ValidUUIDValidator : ConstraintValidator<ValidUUID, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (!value.isNullOrBlank()) {
            try {
                UUID.fromString(value)
            } catch (e: java.lang.IllegalArgumentException) {
                return false
            }
        }
        return true
    }
}