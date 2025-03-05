package ru.elkcl.webprak.entities

import io.hypersistence.utils.hibernate.type.array.ListArrayType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.Type

@Entity
@Table(name = "client_types")
class ClientType(
    @Column(name = "name", nullable = false)
    val name: String,

//    @Type(ListArrayType::class)
//    @Column(
//        name = "info_keys",
//        columnDefinition = "text[] NOT NULL"
//    )
//    val info_keys : List<String>,
) : BaseEntity<Long>()