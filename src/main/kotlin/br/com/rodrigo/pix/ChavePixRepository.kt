package br.com.rodrigo.pix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, UUID> {

    fun existsByChave(chave:String) : Boolean

    fun findByIdAndIdentificadorCliente(id: UUID, clienteId: UUID): Optional<ChavePix>

    fun findByChave(chave: String): Optional<ChavePix>

    fun findByIdentificadorCliente(clienteId: UUID) : List<ChavePix>
}