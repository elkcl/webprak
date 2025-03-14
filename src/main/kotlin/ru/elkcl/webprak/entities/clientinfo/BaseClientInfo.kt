package ru.elkcl.webprak.entities.clientinfo

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.Serializable

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = IndividualClientInfo::class, name = "individual"),
    JsonSubTypes.Type(value = LegalEntityClientInfo::class, name = "legal_entity")
)
sealed interface BaseClientInfo : Serializable {
    fun getType(): String
}