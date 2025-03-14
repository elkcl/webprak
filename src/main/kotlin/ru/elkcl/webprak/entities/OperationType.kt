package ru.elkcl.webprak.entities

import jakarta.persistence.Entity

@Entity
class OperationType(
    val name: String,
) : BaseEntity<Long>()