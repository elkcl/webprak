package ru.elkcl.webprak.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "service_types")
class ServiceType(
    @Column(name = "name", nullable = false)
    val name: String,
) : BaseEntity<Long>()