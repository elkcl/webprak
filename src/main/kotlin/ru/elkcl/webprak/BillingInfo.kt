package ru.elkcl.webprak

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.Serializable

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = OneoffBillingInfo::class),
    JsonSubTypes.Type(value = MonthlyBillingInfo::class)
)
sealed interface BaseBillingInfo : Serializable

class OneoffBillingInfo(
    val amount: Int
) : BaseBillingInfo

class MonthlyBillingInfo(
    val initial: Int,
    val recurring: Int,
) : BaseBillingInfo

enum class BillingType : Serializable {
    ONEOFF,
    MONTHLY
}

fun billingInfo(billingType: BillingType) = when (billingType) {
    BillingType.ONEOFF -> OneoffBillingInfo::class
    BillingType.MONTHLY -> MonthlyBillingInfo::class
}