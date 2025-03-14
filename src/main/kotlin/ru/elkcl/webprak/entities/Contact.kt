package ru.elkcl.webprak.entities

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type

@Entity
class Contact(
    @ManyToOne(fetch = FetchType.LAZY)
    val client: Client,

    val familyName: String,
    val givenName: String,
    val patronymic: String?,
    val email: String?,
    val phone: String?,
    val address: String?,

    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    val other: Map<String, String>?,
) : BaseEntity<Long>()