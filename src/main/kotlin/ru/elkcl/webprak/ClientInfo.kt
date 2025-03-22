package ru.elkcl.webprak

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.Serializable
import java.time.LocalDate

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = IndividualClientInfo::class),
    JsonSubTypes.Type(value = LegalEntityClientInfo::class)
)
sealed interface BaseClientInfo : Serializable
class IndividualClientInfo(
    val passportNumber: String,
    val passportIssueDate: LocalDate,
) : BaseClientInfo

class LegalEntityClientInfo(
    val registryNumber: String,
    val taxpayerNumber: String,
    val address: String,
) : BaseClientInfo

enum class ClientType : Serializable {
    INDIVIDUAL,
    LEGAL_ENTITY,
}

fun clientInfo(clientType: ClientType) = when (clientType) {
    ClientType.INDIVIDUAL -> IndividualClientInfo::class
    ClientType.LEGAL_ENTITY -> LegalEntityClientInfo::class
}