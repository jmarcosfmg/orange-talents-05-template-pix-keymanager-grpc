package br.com.zup.orangetalents

import br.com.zup.orangetalents.handlers.ChaveExistsViolationException
import br.com.zup.orangetalents.handlers.ChaveFormatViolationException
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.Around
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import jakarta.inject.Singleton
import javax.validation.ConstraintViolationException
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*

@Singleton
@InterceptorBean(ErrorAroundHandler::class)
class ErrorHandlerInterceptor : MethodInterceptor<Any, Any> {

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {

        return try {
            context.proceed()
        } catch (ex: Exception) {
            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            val status = when (ex) {
                is ConstraintViolationException -> Status.INVALID_ARGUMENT
                    .withCause(ex)
                    .withDescription(ex.message)
                is ChaveFormatViolationException -> Status.INVALID_ARGUMENT
                    .withDescription(ex.message)
                is ChaveExistsViolationException -> Status.ALREADY_EXISTS
                    .withDescription(ex.message)
                else -> Status.UNKNOWN
                    .withCause(ex)
                    .withDescription("Ops, um erro inesperado ocorreu")
            }
            responseObserver.onError(status.asRuntimeException())
        }
    }

}

@MustBeDocumented
@Retention(RUNTIME)
@Target(CLASS, FIELD, TYPE, FUNCTION)
@Around
annotation class ErrorAroundHandler