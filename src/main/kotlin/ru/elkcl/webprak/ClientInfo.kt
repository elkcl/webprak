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
) : BaseClientInfo {
    override fun toString(): String {
        return "паспорт №$passportNumber, выдан $passportIssueDate"
    }
}

class LegalEntityClientInfo(
    val registryNumber: String,
    val taxpayerNumber: String,
    val address: String,
) : BaseClientInfo {
    override fun toString(): String {
        return "ОГРН: $registryNumber; ИНН: $taxpayerNumber, адрес: $address"
    }
}

enum class ClientType(val desc: String) : Serializable {
    INDIVIDUAL("Физ. лицо"),
    LEGAL_ENTITY("Юр. лицо");

    override fun toString(): String {
        return desc
    }
}

fun clientInfo(clientType: ClientType) = when (clientType) {
    ClientType.INDIVIDUAL -> IndividualClientInfo::class
    ClientType.LEGAL_ENTITY -> LegalEntityClientInfo::class
}