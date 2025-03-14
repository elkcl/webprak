package ru.elkcl.webprak.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Operation(
    @ManyToOne(fetch = FetchType.LAZY)
    val client: Client,

    @ManyToOne(fetch = FetchType.LAZY)
    val operationType: OperationType,

    @ManyToOne(fetch = FetchType.LAZY)
    val service: Service?,

    val timestamp: LocalDateTime,
    val amount: Int,

    @Column(columnDefinition = "text")
    val description: String?,
) : BaseEntity<Long>()