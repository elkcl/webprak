package ru.elkcl.webprak.entities.billinginfo

class MonthlyBillingInfo(
    val initial: Int,
    val recurring: Int,
) : BaseBillingInfo {
    override fun getType(): String {
        return "monthly"
    }
}