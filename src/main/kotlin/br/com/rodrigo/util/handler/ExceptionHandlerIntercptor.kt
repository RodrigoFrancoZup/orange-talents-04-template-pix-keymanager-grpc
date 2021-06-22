package br.com.rodrigo.util.handler

import br.com.rodrigo.util.exception.ChavePixExistenteException
import br.com.rodrigo.util.exception.ChavePixInexistenteException
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import java.lang.IllegalArgumentException
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
//A anotação abaixo indica que vamos interceptar todos Beans com a anotação ExceptionHandler
// vamos intercptar para ver se tem erro ou se deixamos continuar a requisição
@InterceptorBean(ExceptionHandler::class)
class ExceptionHandlerIntercptor : MethodInterceptor<Any, Any> {

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {

        try {

            //Siga o processamento que intercptamos
            return context.proceed()
        } catch (e: ConstraintViolationException) {

            val status = Status.INVALID_ARGUMENT
                .withCause(e)
                .withDescription(e.message)

            val statusRuntimeException = StatusRuntimeException(status)

            //Como conseguir o objeto responseObserver para responder ao cliente?
            // Pois ele nos foi dado ao implementar o servico do .proto

            //Resposta: Vamos conseguir através do contexto. Vai existir um contexto para
            // cada metodo ( ou metodo da classe) que recebeu a anotação @ExceptionHandler,
            // e como sabemos que o responseObserver é sempre o segundo parâmetro de um serviço de gRPC
            // podemos pegar o segundo parâmetro (indice 1) do contexto!

            /*
            Explicando na prática: Anotei a classe RegistraChaveEndpoint com @ExceptionHandler, pois nela há funções que pode
            gerar Exception e a anotação @ExceptionHandler é um "Advice ou Around".

            Por essa classe ter essa anotação ela sempre sera interceptada por este interceptador, no caso
            ExceptionHandlerIntercptor, pois fizemos ele interceptar todos Beans com  @ExceptionHandler.

            O interceptador sempre tem um contexo, que é a AÇÃO que a classe ou método vai fazer que pode gerar Exception,
            neste caso seria a função registraChave() que está dentro da classe RegistraChaveEndpoint.

            Se o contexto nesse caso é a função registraChave() e sabemos que essa função tem como parâmetro
            um responseObserver e que ele é sempre o segundo parâmetro, vamos pegá-lo atavés
            context.parameterValues[1]
             */
            val responseObserver = context.parameterValues[1] as StreamObserver<*>

            responseObserver.onError(statusRuntimeException)
        } catch (e: ChavePixExistenteException) {
            val status = Status.ALREADY_EXISTS
                .withCause(e)
                .withDescription(e.message)

            val statusRuntimeException = StatusRuntimeException(status)
            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(statusRuntimeException)
        } catch (e: IllegalStateException) {
            val status = Status.INVALID_ARGUMENT
                .withCause(e)
                .withDescription(e.message)

            val statusRuntimeException = StatusRuntimeException(status)
            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(statusRuntimeException)
        }catch (e: ChavePixInexistenteException) {
            val status = Status.NOT_FOUND
                .withCause(e)
                .withDescription(e.message)

            val statusRuntimeException = StatusRuntimeException(status)
            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(statusRuntimeException)
        }catch (e: IllegalArgumentException) {
            val status = Status.INVALID_ARGUMENT
                .withCause(e)
                .withDescription(e.message)

            val statusRuntimeException = StatusRuntimeException(status)
            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(statusRuntimeException)
        }catch (e: IllegalAccessException) {
            val status = Status.FAILED_PRECONDITION
                .withCause(e)
                .withDescription(e.message)

            val statusRuntimeException = StatusRuntimeException(status)
            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(statusRuntimeException)
        }//Posso ir colocando mais Catch aqui
        return null
    }
}

