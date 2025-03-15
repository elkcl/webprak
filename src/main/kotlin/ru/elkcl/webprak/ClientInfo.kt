package ru.elkcl.webprak

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.Serializable
import java.time.LocalDate

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

class IndividualClientInfo(
    val passportNumber: String,
    val passportIssueDate: LocalDate,
) : BaseClientInfo {
    override fun getType(): String {
        return "individual"
    }
}

class LegalEntityClientInfo(
    val registryNumber: String,
    val taxpayerNumber: String,
    val address: String,
) : BaseClientInfo {
    override fun getType(): String {
        return "legal_entity"
    }
}