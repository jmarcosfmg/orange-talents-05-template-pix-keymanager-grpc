package br.com.zup.orangetalents.repositories

import br.com.zup.orangetalents.commons.exceptions.ChaveExistsViolationException
import br.com.zup.orangetalents.model.ChavePix
import br.com.zup.orangetalents.model.TipoChave
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.util.*

@Repository
interface ChavePixRepository : CrudRepository<ChavePix, UUID> {

    @Query(value = "SELECT 1 FROM ChavePix p WHERE (p.chave = :chave) AND (p.tipoChave = :tipoChave)")
    fun buscaPorChaveETipo(chave : String, tipoChave : TipoChave): List<Int>

    fun findByChave(chave: String) : Optional<ChavePix>
}

fun ChavePixRepository.insertIfNotExists(chave: ChavePix): ChavePix {
    return this.let {
        chave.takeIf {
            this.buscaPorChaveETipo(it.chave, it.tipoChave)
                .isEmpty()
        }?.run { it.save(this) }
            ?: throw ChaveExistsViolationException("Chave já registrada")
    }
}