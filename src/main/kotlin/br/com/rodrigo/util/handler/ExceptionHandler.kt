package br.com.rodrigo.util.handler

import io.micronaut.aop.Around
import java.lang.annotation.Documented

@Documented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Around
annotation class ExceptionHandler()





