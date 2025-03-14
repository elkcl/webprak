package ru.elkcl.webprak.entities

import jakarta.persistence.Entity

@Entity
class ClientType(
    val name: String,
) : BaseEntity<Long>()