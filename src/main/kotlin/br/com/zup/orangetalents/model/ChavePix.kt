package br.com.zup.orangetalents.model

import br.com.zup.orangetalents.ChavePixRequest
import br.com.zup.orangetalents.TipoChaveGrpc
import com.google.protobuf.Timestamp
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.FutureOrPresent
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class ChavePix(
    @field:NotBlank val idCliente: String,
    @field:NotNull @field:Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(11)") val tipoChave: TipoChave,
    @field:NotNull @field:Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(11)") val tipoConta: TipoConta,
    @field:NotBlank @field:Size(max = 70) var chave: String,
    @field:FutureOrPresent val criadaEm : LocalDateTime = LocalDateTime.now()
) {

    @Id
    val id: UUID = UUID.randomUUID()

    override fun toString(): String {
        return "ChavePix(idCliente='$idCliente', tipoChave=$tipoChave, tipoConta=$tipoConta, chave='$chave')"
    }

    companion object {
        fun fromRequest(request: ChavePixRequest): ChavePix {
            return request.let {
                ChavePix(
                    it.codigo,
                    TipoChave.fromTipoChaveGrpc(it.tipoChave),
                    TipoConta.valueOf(it.tipoConta.name),
                    if (it.tipoChave == TipoChaveGrpc.ALEATORIA) UUID.randomUUID().toString() else it.chave
                )
            }
        }
    }
}

