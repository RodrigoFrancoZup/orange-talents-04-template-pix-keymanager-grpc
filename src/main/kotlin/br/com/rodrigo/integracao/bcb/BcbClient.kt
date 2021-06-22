package br.com.rodrigo.integracao.bcb

import br.com.rodrigo.integracao.bcb.classes.*
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*

import io.micronaut.http.client.annotation.Client

@Client("\${bcb.chaves.url}")
interface BcbClient {

    @Post("/api/v1/pix/keys")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    fun cadastra(@Body createPixKeyRequest: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete("/api/v1/pix/keys/{key}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    fun deleta(@PathVariable key: String, @Body request: DeletePixKeyRequest) : HttpResponse<DeletePixKeyResponse>

    @Get("/api/v1/pix/keys/{key}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    fun busca(@PathVariable key: String) : HttpResponse<PixKeyDetailsResponse>

}

