package ru.elkcl.webprak.entities.clientinfo

import java.time.LocalDate

class IndividualClientInfo(
    val passportNumber: String,
    val passportIssueDate: LocalDate,
) : BaseClientInfo {
    override fun getType(): String {
        return "individual"
    }
}