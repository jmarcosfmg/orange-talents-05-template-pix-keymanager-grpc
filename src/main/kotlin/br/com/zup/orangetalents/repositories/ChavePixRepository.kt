package br.com.zup.orangetalents.repositories

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
}