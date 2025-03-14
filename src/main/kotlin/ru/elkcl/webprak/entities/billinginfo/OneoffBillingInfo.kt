package ru.elkcl.webprak.entities.billinginfo

class OneoffBillingInfo(
    val amount: Int
) : BaseBillingInfo {
    override fun getType(): String {
        return "oneoff"
    }
}