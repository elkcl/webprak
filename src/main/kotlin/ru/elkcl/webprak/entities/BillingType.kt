package ru.elkcl.webprak.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "billing_types")
class BillingType(
    @Column(name = "name", nullable = false)
    val name: String,
) : BaseEntity<Long>()