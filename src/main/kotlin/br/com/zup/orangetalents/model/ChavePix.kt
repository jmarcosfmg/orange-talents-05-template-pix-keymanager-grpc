package br.com.zup.orangetalents.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class ChavePix(
    @field:NotBlank val idCliente: String,
    @field:NotNull @field:Enumerated(EnumType.STRING) @Column(columnDefinition = "VARCHAR(11)") val tipoChave: TipoChave,
    @field:NotNull @field:Enumerated(EnumType.STRING) @Column(columnDefinition = "VARCHAR(11)") val tipoConta: TipoConta,
    @field:NotBlank @field:Size(max = 70) val chave: String
) {

    @Id
    val id: UUID = UUID.randomUUID()
    override fun toString(): String {
        return "ChavePix(idCliente='$idCliente', tipoChave=$tipoChave, tipoConta=$tipoConta, chave='$chave')"
    }
}

