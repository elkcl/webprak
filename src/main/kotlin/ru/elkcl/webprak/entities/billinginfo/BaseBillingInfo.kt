package ru.elkcl.webprak.entities.billinginfo

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.Serializable

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = OneoffBillingInfo::class, name = "oneoff"),
    JsonSubTypes.Type(value = MonthlyBillingInfo::class, name = "monthly")
)
sealed interface BaseBillingInfo : Serializable {
    fun getType(): String
}