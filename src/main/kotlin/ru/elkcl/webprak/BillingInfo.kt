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
) : BaseBillingInfo {
    override fun toString(): String {
        return "$amount руб. единоразово"
    }
}

class MonthlyBillingInfo(
    val initial: Int,
    val recurring: Int,
) : BaseBillingInfo {
    override fun toString(): String {
        return "$initial руб. подключение, $recurring руб./мес"
    }
}

enum class BillingType(val desc: String) : Serializable {
    ONEOFF("Единоразовый"),
    MONTHLY("Ежемесячный");

    override fun toString(): String {
        return desc
    }
}

fun billingInfo(billingType: BillingType) = when (billingType) {
    BillingType.ONEOFF -> OneoffBillingInfo::class
    BillingType.MONTHLY -> MonthlyBillingInfo::class
}