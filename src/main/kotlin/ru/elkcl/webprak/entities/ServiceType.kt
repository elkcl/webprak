package ru.elkcl.webprak.entities

import jakarta.persistence.Entity

@Entity
class ServiceType(
    val name: String,
) : BaseEntity<Long>()