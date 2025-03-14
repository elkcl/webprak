package ru.elkcl.webprak.entities

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import ru.elkcl.webprak.entities.clientinfo.BaseClientInfo
import java.time.LocalDate

@Entity
class Client(
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    val clientType: ClientType,

    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    val clientInfo: BaseClientInfo,

    val balance: Int,
    val creditLimit: Int,
    val creditDue: LocalDate,
) : BaseEntity<Long>()