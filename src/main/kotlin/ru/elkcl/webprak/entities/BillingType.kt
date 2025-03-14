package ru.elkcl.webprak.entities

import jakarta.persistence.Entity

@Entity
class BillingType(
    val name: String,
) : BaseEntity<Long>()